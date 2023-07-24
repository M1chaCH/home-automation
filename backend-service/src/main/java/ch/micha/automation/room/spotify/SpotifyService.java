package ch.micha.automation.room.spotify;

import ch.micha.automation.room.errorhandling.exceptions.SpotifyAlreadyAuthorizedException;
import ch.micha.automation.room.errorhandling.exceptions.SpotifyNotAuthorizedException;
import ch.micha.automation.room.events.OnAppStartupListener;
import ch.micha.automation.room.spotify.dtos.*;
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
    private static final int CHANGE_SONG_DURATION = 400;
    private final Logger logger = Logger.getLogger(getClass().getSimpleName());

    private final SpotifyProvider provider;
    private final SpotifyApiWrapper api;

    @Inject
    public SpotifyService(SpotifyProvider provider, SpotifyApiWrapper api) {
        this.provider = provider;
        this.api = api;
    }

    /**
     * makes sure that the stored auth is refreshed, if no auth stored nothing happens
     * also makes sure that the SpotifyApiWrapper is initialized with the correct auth
     */
    @Override
    public void onAppStartup() {
        try {
            SpotifyAuthorisationDTO refreshedAccess = refreshTokenIfExpired(getAccess());
            api.init(refreshedAccess);
        } catch (SpotifyNotAuthorizedException e) {
            logger.log(Level.WARNING, "did not initialize spotify. Not authorized.");
        }
    }

    /**
     * checks if anything is playing, then toggles the player state
     * if no player is active then nothing happens (only resumes, never starts)
     */
    public void togglePlayback() {
        if(api.getPlayerState().equals(SpotifyPlayerState.PLAYING))
            api.pausePlayback();
        else
            api.resumePlayback();
    }

    /**
     * starts a context, also sets the player to shuffle
     * @param context the volume and the url to start
     */
    public void startContext(SpotifyContextDTO context) {
        api.playContext(context.getContext());
        api.setPlaybackVolume(context.getVolume());
        api.setPlaybackShuffle(true);
    }

    /**
     * checks the current player state, if no player is active then starts at the given context, else just resumes the
     * player
     * @param context the volume and the url to start
     */
    public void resumePlayerOrStartContext(SpotifyContextDTO context) {
        if(api.getPlayerState().equals(SpotifyPlayerState.STOPPED))
            startContext(context);
        else
            api.resumePlayback();
    }

    /**
     * does nothing but pauses the playback
     * if nothing is playing then nothing happens
     */
    public void pausePlayback() {
        api.pausePlayback();
    }

    public void setVolume(int volume) {
        api.setPlaybackVolume(volume);
    }

    /**
     * skips to the next song and loads the future player. Needs to wait a bit to make sure the player on spotify side
     * has updated, otherwise the new player won't be updated
     * @return the player with the next song
     * @throws InterruptedException if the wait is interrupted
     */
    public SpotifyPlayerDTO skipToNextSong() throws InterruptedException {
        this.api.nextSong();
        Thread.sleep(CHANGE_SONG_DURATION);
        return this.api.getPlayer();
    }

    /**
     * moves to the previous song and loads the future player. Needs to wait a bit to make sure the player on spotify side
     * has updated, otherwise the new player won't be updated
     * @return the player with the previous song
     * @throws InterruptedException if the wait is interrupted
     */
    public SpotifyPlayerDTO previousSong() throws InterruptedException {
        this.api.previousSong();
        Thread.sleep(CHANGE_SONG_DURATION);
        return this.api.getPlayer();
    }

    /**
     * @return spotify resources (first 50 liked or owned playlists of current user)
     */
    public synchronized List<SpotifyResourceDTO> loadResources() {
        return api.getSavedSpotifyResources();
    }

    /**
     * @return the current spotify player, null if none active
     */
    public SpotifyPlayerDTO loadCurrentPlayer() {
        return api.getPlayer();
    }

    /**
     * configure a new auth token set
     * only needs to be done once after first authorisation
     * also re-initializes the api instance
     * @param dto the new auth token set
     * @throws SpotifyAlreadyAuthorizedException if a second auth set was tried to be added
     */
    public void addAuthorisation(SpotifyCodeDTO dto) {
        SpotifyClientDTO client = getClient();
        SpotifyAuthorisationDTO auth = api.requestAccessToken(dto.getCode(), dto.getRedirectUrl(), client);
        if(provider.insertAuth(auth))
            api.init(getAccess());
        else
            throw new SpotifyAlreadyAuthorizedException();
    }

    /**
     * @return the current auth token set to access spotify (best to not send this to the client)
     */
    public SpotifyAuthorisationDTO getAccess() {
        return provider.findAuth().orElseThrow(SpotifyNotAuthorizedException::new);
    }

    /**
     * @return the "credentials" to the spotify developer account (treat with caution)
     */
    public SpotifyClientDTO getClient() {
        return provider.getClient();
    }

    /**
     * wrapper for {@link #refreshTokenIfExpired(SpotifyAuthorisationDTO)}
     */
    public void refreshTokenIfExpired() {
        refreshTokenIfExpired(null);
    }

    /**
     * runs the refreshTokenIfExpired method from the API, if an update was done then updates the token in the DB
     * @param auth the current auth token set (optional - if not present uses cached one from api, used on initialize
     *             when api has nothing cached)
     * @return the current & working token set
     */
    private synchronized SpotifyAuthorisationDTO refreshTokenIfExpired(SpotifyAuthorisationDTO auth) {
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

    /**
     * expects the api to always be initialized if auth is configured
     * @return true: the api is initialized and ready to use
     */
    public boolean isSpotifyAuthorized() {
        return api.isInitialized();
    }
}
