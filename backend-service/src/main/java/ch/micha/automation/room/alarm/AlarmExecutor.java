package ch.micha.automation.room.alarm;

import ch.micha.automation.room.scene.SceneEntity;
import ch.micha.automation.room.scene.SceneService;
import ch.micha.automation.room.spotify.SpotifyService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.Setter;

@Setter
@ApplicationScoped
public class AlarmExecutor implements Runnable{
    public static final int ALARM_INITIAL_VOLUME = 10; // (spotify player) percent
    public static final int ALARM_VOLUME_INCREASE_TIMEOUT = 15; // seconds
    public static final int ALARM_VOLUME_INCREASE_STEP = 5; // (spotify player) percent

    private static final Logger LOGGER = Logger.getLogger(AlarmExecutor.class.getSimpleName());

    private final SceneService sceneService;
    private final SpotifyService spotifyService;

    private AlarmEntity alarmToRun;
    private SceneEntity sceneToRun;
    private int maxAlarmVolume;

    @Inject
    public AlarmExecutor(SceneService sceneService, SpotifyService spotifyService) {
        this.sceneService = sceneService;
        this.spotifyService = spotifyService;
    }

    @Override
    public void run() {
        try {
            if(alarmToRun == null)
                throw new IllegalStateException("alarm to run not defined!");
            if(sceneToRun == null)
                throw new IllegalStateException("scene for alarm to run not defined!");

            LOGGER.log(Level.INFO, "running alarm planned at {0} with scene {1}:{2}",
                new Object[]{ alarmToRun.cronSchedule(), sceneToRun.id(), sceneToRun.name() });

            maxAlarmVolume = sceneToRun.spotifyVolume();
            sceneToRun = new SceneEntity( // because records are immutable 🤦‍♂️
                sceneToRun.id(),
                sceneToRun.name(),
                sceneToRun.defaultScene(),
                sceneToRun.lights(),
                sceneToRun.spotifyResource(),
                ALARM_INITIAL_VOLUME
            );

            sceneService.applyScene(sceneToRun, true);
            LOGGER.log(Level.INFO, "started scene for alarm at initial volume of {0}",
                new Object[]{ ALARM_INITIAL_VOLUME });

            for (int currentVolume = ALARM_INITIAL_VOLUME; currentVolume <= maxAlarmVolume; currentVolume += ALARM_VOLUME_INCREASE_STEP) {
                spotifyService.setVolume(currentVolume);
                LOGGER.log(Level.INFO, "increased volume of alarm to {0}", new Object[]{ currentVolume });
                Thread.sleep(ALARM_VOLUME_INCREASE_TIMEOUT * 1000L);
            }

            LOGGER.log(Level.INFO, "completed volume increase of alarm, nothing to do anymore");
        } catch (InterruptedException interrupted) {
            LOGGER.log(Level.INFO, "alarm executor interrupted -> reinterpreting", new Object[]{ });
            Thread.currentThread().interrupt();
        }
    }

    public void stopAlarm() {
       sceneService.shutdown();
    }

    public void applyFinalVolume() {
        spotifyService.setVolume(maxAlarmVolume);
    }
}
