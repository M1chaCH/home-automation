package ch.micha.automation.room.scene;

import ch.micha.automation.room.errorhandling.exceptions.ResourceNotFoundException;
import ch.micha.automation.room.light.yeelight.YeelightDeviceService;
import ch.micha.automation.room.spotify.SpotifyApiService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class SceneService {
    private final Logger logger = Logger.getLogger(getClass().getSimpleName());
    private final SceneProvider sceneProvider;
    private final YeelightDeviceService yeelightDeviceService;
    private final SpotifyApiService spotifyService;

    @Inject
    public SceneService(SceneProvider sceneProvider, YeelightDeviceService yeelightDeviceService, SpotifyApiService spotifyService) {
        this.sceneProvider = sceneProvider;
        this.yeelightDeviceService = yeelightDeviceService;
        this.spotifyService = spotifyService;
    }

    public void applyScene(int sceneId){
        Optional<SceneEntity> scene = sceneProvider.findSceneById(sceneId);
        if(scene.isEmpty())
            throw new ResourceNotFoundException("scene", "" + sceneId);

        applyScene(scene.get());
    }

    public void applyDefaultScene() {
        applyScene(sceneProvider.loadDefaultScene());
    }

    public void applyScene(SceneEntity scene) {
        logger.log(Level.INFO, "applying scene {0}-{1}", new Object[]{ scene.id(), scene.name()});

        scene.lights().forEach(yeelightDeviceService::applyConfigToLight);

        logger.log(Level.INFO, "applied scene");
    }

    /**
     * prepares the room to be left alone. (alarms will also be disabled, will be enabled on the next entry)
     */
    public void shutdown() {
        yeelightDeviceService.powerAllOff();
        spotifyService.pausePlayback();
    }
}
