package ch.micha.automation.room.spotify;

import ch.micha.automation.room.errorhandling.exceptions.SpotifyAlreadyAuthorizedException;
import ch.micha.automation.room.errorhandling.exceptions.SpotifyNotAuthorizedException;
import ch.micha.automation.room.events.OnAppStartupListener;
import ch.micha.automation.room.spotify.dtos.SpotifyAuthorisationDTO;
import ch.micha.automation.room.spotify.dtos.SpotifyClientDTO;
import ch.micha.automation.room.spotify.dtos.SpotifyCodeDTO;
import ch.micha.automation.room.spotify.dtos.SpotifyResourceDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This service calls the spotify player API to play or search specific music.<br>
 * Some helpful links:
 * https://developer.spotify.com/documentation/general/guides/authorization/code-flow/
 * https://developer.spotify.com/documentation/web-api/reference/#/
 */
@ApplicationScoped
public class SpotifyService implements OnAppStartupListener {
    private final Logger logger = Logger.getLogger(getClass().getSimpleName());

    private final SpotifyProvider provider;
    private final SpotifyApiService api;

    @Inject
    public SpotifyService(SpotifyProvider provider, SpotifyApiService api) {
        this.provider = provider;
        this.api = api;
    }

    @Override
    public void onAppStartup() {
        try {
            SpotifyAuthorisationDTO refreshedAccess = refreshTokenIfExpired(getAccess());
            api.init(refreshedAccess);
        } catch (SpotifyNotAuthorizedException e) {
            logger.log(Level.WARNING, "did not initialize spotify. Not authorized.");
        }
    }

    public void togglePlayback() {
        refreshTokenIfExpired();
        if(api.isPlaying())
            api.pausePlayback();
        else
            api.resumePlayback();
    }

    public void startContext(String contextUri, int volume) {
        refreshTokenIfExpired();
        api.setPlaybackVolume(volume);
        api.setPlaybackShuffle(true);
        api.playContext(contextUri);
    }

    public void pausePlayback() {
        refreshTokenIfExpired();
        api.pausePlayback();
    }

    public List<SpotifyResourceDTO> loadResources() {
        refreshTokenIfExpired();
        return api.getSavedSpotifyResources();
    }

    public void addAuthorisation(SpotifyCodeDTO dto) {
        SpotifyClientDTO client = getClient();
        SpotifyAuthorisationDTO auth = api.requestAccessToken(dto.getCode(), dto.getRedirectUrl(), client);
        if(provider.insertAuth(auth))
            api.init(getAccess());
        else
            throw new SpotifyAlreadyAuthorizedException();
    }

    public SpotifyAuthorisationDTO getAccess() {
        return provider.findAuth().orElseThrow(SpotifyNotAuthorizedException::new);
    }

    public SpotifyClientDTO getClient() {
        return provider.getClient();
    }

    private void refreshTokenIfExpired() {
        refreshTokenIfExpired(null);
    }

    private SpotifyAuthorisationDTO refreshTokenIfExpired(SpotifyAuthorisationDTO auth) {
        SpotifyAuthorisationDTO newAuth;
        if(auth != null)
            newAuth = api.refreshTokenIfExpired(auth, getClient());
        else
            newAuth = api.refreshTokenIfExpired(getClient());

        if(newAuth != null) {
            provider.updateAuth(newAuth);
            return newAuth;
        }
        return auth;
    }

    public boolean isSpotifyAuthorized() {
        return api.isInitialized();
    }
}
