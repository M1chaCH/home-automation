package ch.micha.automation.room.automation;

import ch.micha.automation.room.alarm.AlarmTrigger;
import ch.micha.automation.room.errorhandling.SceneApplyResponseDTO;
import ch.micha.automation.room.light.configuration.LightConfig;
import ch.micha.automation.room.light.yeelight.YeelightDeviceEntity;
import ch.micha.automation.room.light.yeelight.YeelightDeviceService;
import ch.micha.automation.room.scene.SceneService;
import ch.micha.automation.room.spotify.SpotifyService;
import ch.micha.automation.room.spotify.dtos.SpotifyPlayerDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class RoomAutomationService {
    private final Logger logger = Logger.getLogger(getClass().getSimpleName());
    private final SpotifyService spotify;
    private final YeelightDeviceService devices;
    private final SceneService scenes;
    private final AlarmTrigger alarmTrigger;

    private Map<YeelightDeviceEntity, LightConfig> lastLightConfigs;
    private SpotifyPlayerDTO lastPlayer = null;

    @Inject
    public RoomAutomationService(SpotifyService spotify, YeelightDeviceService devices, SceneService scenes, AlarmTrigger alarmTrigger) {
        this.spotify = spotify;
        this.devices = devices;
        this.scenes = scenes;
        this.alarmTrigger = alarmTrigger;
    }

    /**
     * toggles the room by an internal state.
     * when first start ever, starts default scene
     * when stopping, saves the current light state, when restarting after that applies this "saved" light state
     * spotify either continues at last left of state or starts default scene audio
     */
    public ToggleRoomResponseDTO toggleRoom() {
        ToggleRoomResponseDTO response = new ToggleRoomResponseDTO();

        // stop alarm if running
        if(alarmTrigger.isAlarmRunning()) {
            logger.log(Level.INFO, "smart toggle decision: stop alarm");
            alarmTrigger.stopCurrentAlarm();
            response.setOn(false);
            response.setSuccess(true);
            return response;
        }

        if(assumeRoomOff()) {
            logger.log(Level.INFO, "smart toggle decision: toggling room ON");
            response.setOn(true);

            if(lastLightConfigs == null) {
                SceneApplyResponseDTO sceneResponse = scenes.applyDefaultScene();
                response.setSuccess(!sceneResponse.isFailed());
            } else {
                try {
                    // since the connection of the stored devices might expire,
                    // we need to make sure that we get a "connected" instance of the light
                    lastLightConfigs.forEach((device, config) -> {
                        device = devices.getConnectedLight(device.ip());
                        devices.applyConfigToLight(device, config);
                    });

                    if(lastPlayer != null && lastPlayer.isPlaying()) {
                        spotify.resumePlayerOrStartContext(lastPlayer.getContext());
                    }
                    response.setSuccess(true);
                } catch (Exception e) {
                    response.setSuccess(false);
                    logger.log(Level.INFO, "failed to 'resume' room", e);
                }
            }
        } else {
            logger.log(Level.INFO, "smart toggle decision: toggling room OFF");
            response.setOn(false);
            lastLightConfigs = devices.loadCurrentOnlineConfigs();
            lastPlayer = spotify.loadCurrentPlayer();

            try {
                scenes.shutdown();
                response.setSuccess(true);
            } catch (Exception e) {
                response.setSuccess(false);
                logger.log(Level.INFO, "failed to shutdown room", e);
            }
        }

        return response;
    }

    private boolean assumeRoomOff() {
        Map<YeelightDeviceEntity, LightConfig> configs = devices.loadCurrentOnlineConfigs();
        int amountRunning = 0;
        int amountStopped = 0;
        for (Entry<YeelightDeviceEntity, LightConfig> entry : configs.entrySet()) {
            if(entry.getValue() == null) amountStopped++;
            else amountRunning++;
        }
        return amountRunning <= amountStopped;
    }
}
