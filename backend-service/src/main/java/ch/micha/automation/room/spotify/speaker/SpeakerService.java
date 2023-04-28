package ch.micha.automation.room.spotify.speaker;

import ch.micha.automation.room.errorhandling.exceptions.SpeakerRestartException;
import com.jcraft.jsch.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class SpeakerService {
    private static final int DEFAULT_SSH_PORT = 22;
    private static final int DEFAULT_SSH_TIMEOUT = 5 * 1000;
    private static final String DEFAULT_PROPERTY_VALUE = "undefined";
    private final Logger logger = Logger.getLogger(SpeakerService.class.getSimpleName());

    private final String speakerHost;
    private final String sshUsername;
    private final String sshPassword;
    private final String speakerRestartCommand;

    @Inject
    public SpeakerService(@ConfigProperty(name = "room.automation.speaker.host", defaultValue = DEFAULT_PROPERTY_VALUE)
                              String speakerHost,
                          @ConfigProperty(name = "room.automation.speaker.user", defaultValue = DEFAULT_PROPERTY_VALUE)
                          String sshUsername,
                          @ConfigProperty(name = "room.automation.speaker.password", defaultValue = DEFAULT_PROPERTY_VALUE)
                              String sshPassword,
                          @ConfigProperty(name = "room.automation.speaker.command", defaultValue = DEFAULT_PROPERTY_VALUE)
                              String speakerRestartCommand) {
        this.speakerHost = speakerHost;
        this.sshUsername = sshUsername;
        this.sshPassword = sshPassword;
        this.speakerRestartCommand = speakerRestartCommand;
    }

    /**
     * tries to restart the configured speaker device
     * if no device is configured nothing happens
     * @throws SpeakerRestartException if an error in the process occurs
     */
    public void restartSpeaker() {
        if(DEFAULT_PROPERTY_VALUE.equals(speakerHost)
                || DEFAULT_PROPERTY_VALUE.equals(sshUsername)
                || DEFAULT_PROPERTY_VALUE.equals(sshPassword)
                || DEFAULT_PROPERTY_VALUE.equals(speakerRestartCommand)) {
            logger.log(Level.INFO, "not all properties are configured, not restarting speaker");
        }

        try {
            logger.log(Level.INFO, "restarting speaker at {0}:{1}", new Object[]{speakerHost, DEFAULT_SSH_PORT});
            Session session = new JSch().getSession(sshUsername, speakerHost, DEFAULT_SSH_PORT);
            session.setPassword(sshPassword);
            session.setConfig("StrictHostKeyChecking", "no");
            session.setTimeout(DEFAULT_SSH_TIMEOUT);
            session.connect();

            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(speakerRestartCommand);
            logger.log(Level.INFO, "using restart command: {0}", speakerRestartCommand);

            InputStream channelError = channel.getErrStream();
            channel.connect();
            String response = new String(channelError.readAllBytes(), StandardCharsets.UTF_8);
            channel.disconnect();
            session.disconnect();

            if(!response.isEmpty() && !response.isBlank()) {
                logger.log(Level.WARNING, "speaker responded with error: {0}", response);
                throw new SpeakerRestartException("Speaker restart command failed", speakerHost);
            }

            logger.log(Level.INFO, "successfully executed command");
        } catch (JSchException | IOException e) {
            if(e.getMessage().contains("timeout"))
                throw new SpeakerRestartException("Speaker connection failed", speakerHost);
            if(e.getMessage().contains("Auth fail"))
                throw new SpeakerRestartException("Speaker login failed", speakerHost);

            logger.log(Level.WARNING, "speaker failed unexpected", e);
            throw new SpeakerRestartException("unexpected error", speakerHost);
        }
    }
}
