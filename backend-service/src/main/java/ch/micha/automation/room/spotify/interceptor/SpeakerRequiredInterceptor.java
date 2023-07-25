package ch.micha.automation.room.spotify.interceptor;

import ch.micha.automation.room.errorhandling.exceptions.ResourceNotFoundException;
import ch.micha.automation.room.spotify.speaker.SpeakerService;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import java.util.logging.Level;
import java.util.logging.Logger;

@SpeakerRequired
@Interceptor
public class SpeakerRequiredInterceptor {
    private static final Logger LOGGER = Logger.getLogger(SpeakerRequiredInterceptor.class.getSimpleName());

    private final SpeakerService speakerService;

    @Inject
    public SpeakerRequiredInterceptor(SpeakerService speakerService) {
        this.speakerService = speakerService;
    }

    /**
     * executed around every method that is annotated with @SpeakerRequired
     * if the speaker was not found by spotify, then we try to restart the speaker
     * @param invocationContext context containing info about the invocation process
     * @return the return type of the method that will be invoked
     * @throws Exception if ever, anything goes wrong (: (kind of "redirect all errors, if any occur")
     */
    @AroundInvoke
    public Object intercept(InvocationContext invocationContext) throws Exception {
        try {
            return invocationContext.proceed();
        } catch (ResourceNotFoundException e) {
            if(e.getMessage().contains("spotify speaker")){
                LOGGER.log(Level.INFO, "caught spotify speaker not found, restarting speaker & retrying", new Object[]{ });
                speakerService.restartSpeaker();
                Thread.sleep(1500); // wait for the speaker to boot and connect to spotify
                return invocationContext.proceed();
            } else
                throw e;
        }
    }
}
