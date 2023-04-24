package ch.micha.automation.room;

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

    public void toggleRoom() {
        if(!roomOn) {
            logger.log(Level.INFO, "toggling room ON");

            if(currentLightConfigs == null) {
                scenes.applyDefaultScene();
            } else {
                currentLightConfigs.forEach(devices::applyConfigToLight);
                spotify.resumePlayerOrStartContext(scenes.getDefaultSpotifyContext());
            }
        } else {
            logger.log(Level.INFO, "toggling room OFF");
            currentLightConfigs = devices.loadCurrentOnlineConfigs();

            scenes.shutdown();
        }
        roomOn = !roomOn;
    }
}
