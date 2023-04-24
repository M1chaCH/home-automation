package ch.micha.automation.room.spotify.interceptor;

import ch.micha.automation.room.spotify.SpotifyService;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

@SpotifyAuthorized
@Interceptor
public class SpotifyAuthorisationInterceptor {

    private final SpotifyService spotifyService;

    @Inject
    public SpotifyAuthorisationInterceptor(SpotifyService spotifyService) {
        this.spotifyService = spotifyService;
    }

    @AroundInvoke
    public Object intercept(InvocationContext invocationContext) throws Exception {
        spotifyService.refreshTokenIfExpired();
        return invocationContext.proceed();
    }
}
