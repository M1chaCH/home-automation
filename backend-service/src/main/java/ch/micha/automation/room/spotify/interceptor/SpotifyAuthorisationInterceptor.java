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

    /**
     * executed around every method that is annotated with @SpotifyAuthorized
     * @param invocationContext context containing info about the invocation process
     * @return the return type of the method that will be invoked
     * @throws Exception if ever anything goes wrong (: (kind of "redirect all errors, if any occur")
     */
    @AroundInvoke
    public Object intercept(InvocationContext invocationContext) throws Exception {
        spotifyService.refreshTokenIfExpired();
        return invocationContext.proceed();
    }
}
