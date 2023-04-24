package ch.micha.automation.room.spotify.interceptor;

import ch.micha.automation.room.spotify.SpotifyService;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import java.util.logging.Level;
import java.util.logging.Logger;

@SpotifyAuthorized
@Interceptor
public class SpotifyAuthorisationInterceptor {

    private final Logger logger = Logger.getLogger(getClass().getSimpleName());
    private final SpotifyService spotifyService;

    @Inject
    public SpotifyAuthorisationInterceptor(SpotifyService spotifyService) {
        this.spotifyService = spotifyService;
    }

    @AroundInvoke
    public Object intercept(InvocationContext invocationContext) throws Exception {
        logger.log(Level.INFO, "checking spotify auth token");
        spotifyService.refreshTokenIfExpired();
        return invocationContext.proceed();
    }
}
