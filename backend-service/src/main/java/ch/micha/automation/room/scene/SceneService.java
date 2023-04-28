package ch.micha.automation.room.scene;

import ch.micha.automation.room.errorhandling.exceptions.ResourceNotFoundException;
import ch.micha.automation.room.events.EventHandlerPriority;
import ch.micha.automation.room.events.HandlerPriority;
import ch.micha.automation.room.events.OnAppStartupListener;
import ch.micha.automation.room.light.yeelight.YeelightDeviceService;
import ch.micha.automation.room.spotify.SpotifyService;
import ch.micha.automation.room.spotify.dtos.SpotifyContextDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class SceneService implements OnAppStartupListener {
    private final Logger logger = Logger.getLogger(getClass().getSimpleName());
    private final SceneProvider sceneProvider;
    private final YeelightDeviceService yeelightDeviceService;
    private final SpotifyService spotifyService;

    @Inject
    public SceneService(SceneProvider sceneProvider, YeelightDeviceService yeelightDeviceService, SpotifyService spotifyService) {
        this.sceneProvider = sceneProvider;
        this.yeelightDeviceService = yeelightDeviceService;
        this.spotifyService = spotifyService;
    }

    @Override
    @EventHandlerPriority(HandlerPriority.LAST)
    public void onAppStartup() {
        sceneProvider.loadDefaultScene();
    }

    public void applyScene(int sceneId){
        Optional<SceneEntity> scene = sceneProvider.findSceneById(sceneId);
        if(scene.isEmpty())
            throw new ResourceNotFoundException("scene", String.valueOf(sceneId));

        applyScene(scene.get());
    }

    public void applyDefaultScene() {
        applyScene(sceneProvider.loadDefaultScene());
    }

    public void applyScene(SceneEntity scene) {
        logger.log(Level.INFO, "applying scene {0}-{1}", new Object[]{ scene.id(), scene.name()});

        scene.lights().forEach(yeelightDeviceService::applyConfigToLight);

        if(scene.spotifyResource() != null && !scene.spotifyResource().isEmpty()) {
            spotifyService.startContext(new SpotifyContextDTO(scene.spotifyResource(), scene.spotifyVolume()));
        }

        logger.log(Level.INFO, "applied scene");
    }

    public void addDeviceToDefaultScene(int id) {
        sceneProvider.addDeviceToScene(id, 0, 0);
    }

    /**
     * loads the default scene and extracts its spotify context
     * @return the spotify context from the default scene
     */
    public SpotifyContextDTO getDefaultSpotifyContext() {
        SceneEntity scene = sceneProvider.loadDefaultScene();
        return new SpotifyContextDTO(scene.spotifyResource(), scene.spotifyVolume());
    }

    /**
     * prepares the room to be left alone.
     */
    public void shutdown() {
        yeelightDeviceService.powerAllOff();
        if(spotifyService.isSpotifyAuthorized()) {
            spotifyService.pausePlayback();
        }
    }
}
