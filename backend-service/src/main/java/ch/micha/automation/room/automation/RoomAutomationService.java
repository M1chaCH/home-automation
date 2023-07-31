package ch.micha.automation.room.automation;

import ch.micha.automation.room.errorhandling.SceneApplyResponseDTO;
import ch.micha.automation.room.light.configuration.LightConfig;
import ch.micha.automation.room.light.yeelight.YeelightDeviceEntity;
import ch.micha.automation.room.light.yeelight.YeelightDeviceService;
import ch.micha.automation.room.scene.SceneService;
import ch.micha.automation.room.spotify.SpotifyService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class RoomAutomationService {
    private final Logger logger = Logger.getLogger(getClass().getSimpleName());
    private final SpotifyService spotify;
    private final YeelightDeviceService devices;
    private final SceneService scenes;

    private boolean roomOn = false;

    private Map<YeelightDeviceEntity, LightConfig> currentLightConfigs;

    @Inject
    public RoomAutomationService(SpotifyService spotify, YeelightDeviceService devices, SceneService scenes) {
        this.spotify = spotify;
        this.devices = devices;
        this.scenes = scenes;
    }

    /**
     * toggles the room by an internal state.
     * when first start ever, starts default scene
     * when stopping, saves the current light state, when restarting after that applies this "saved" light state
     * spotify either continues at last left of state or starts default scene audio
     */
    public ToggleRoomResponseDTO toggleRoom() {
        ToggleRoomResponseDTO response = new ToggleRoomResponseDTO();
        if(!roomOn) {
            logger.log(Level.INFO, "toggling room ON");

            if(currentLightConfigs == null) {
                SceneApplyResponseDTO sceneResponse = scenes.applyDefaultScene();
                response.setSuccess(!sceneResponse.isFailed());
            } else {
                try {
                    // since the connection of the stored devices might expire,
                    // we need to make sure that we get a "connected" instance of the light
                    currentLightConfigs.forEach((device, config) -> {
                        device = devices.getConnectedLight(device.ip());
                        devices.applyConfigToLight(device, config);
                    });
                    spotify.resumePlayerOrStartContext(scenes.getDefaultSpotifyContext());
                    response.setSuccess(true);
                } catch (Exception e) {
                    response.setSuccess(false);
                    logger.log(Level.INFO, "failed to 'resume' room", e);
                }
            }
        } else {
            logger.log(Level.INFO, "toggling room OFF");
            currentLightConfigs = devices.loadCurrentOnlineConfigs();

            try {
                scenes.shutdown();
                response.setSuccess(true);
            } catch (Exception e) {
                response.setSuccess(false);
                logger.log(Level.INFO, "failed to shutdown room", e);
            }
        }
        roomOn = !roomOn;
        response.setOn(roomOn);
        return response;
    }
}
