package ch.micha.automation.room.spotify;

import ch.micha.automation.room.errorhandling.exceptions.SpotifyAlreadyAuthorizedException;
import ch.micha.automation.room.errorhandling.exceptions.SpotifyNotAuthorizedException;
import ch.micha.automation.room.spotify.dtos.SpotifyAuthorisationDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * This service calls the spotify player API to play or search specific music.<br>
 * Some helpful links:
 * https://developer.spotify.com/documentation/general/guides/authorization/code-flow/
 * https://developer.spotify.com/documentation/web-api/reference/#/
 */
@ApplicationScoped
public class SpotifyService {
    private final SpotifyProvider provider;

    @Inject
    public SpotifyService(SpotifyProvider provider) {
        this.provider = provider;
    }

    public SpotifyAuthorisationDTO getAccess() {
        return provider.findAuth().orElseThrow(SpotifyNotAuthorizedException::new);
    }

    public void addAuthorisation(SpotifyAuthorisationDTO dto) {
        if(!provider.insertAuth(dto))
            throw new SpotifyAlreadyAuthorizedException();
    }
}
