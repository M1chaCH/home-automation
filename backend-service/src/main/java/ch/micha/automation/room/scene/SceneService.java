package ch.micha.automation.room.scene;

import ch.micha.automation.room.errorhandling.SceneApplyResponseDTO;
import ch.micha.automation.room.errorhandling.exceptions.AppException;
import ch.micha.automation.room.errorhandling.exceptions.ResourceNotFoundException;
import ch.micha.automation.room.events.EventHandlerPriority;
import ch.micha.automation.room.events.HandlerPriority;
import ch.micha.automation.room.events.OnAppStartupListener;
import ch.micha.automation.room.light.configuration.LightConfig;
import ch.micha.automation.room.light.yeelight.YeelightDeviceEntity;
import ch.micha.automation.room.light.yeelight.YeelightDeviceService;
import ch.micha.automation.room.spotify.SpotifyService;
import ch.micha.automation.room.spotify.dtos.SpotifyContextDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.Map.Entry;
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

    public SceneApplyResponseDTO applyScene(int sceneId){
        Optional<SceneEntity> scene = sceneProvider.findSceneById(sceneId);
        if(scene.isEmpty())
            throw new ResourceNotFoundException("scene", String.valueOf(sceneId));

        return applyScene(scene.get());
    }

    public SceneApplyResponseDTO applyDefaultScene() {
        return applyScene(sceneProvider.loadDefaultScene());
    }

    public SceneApplyResponseDTO applyScene(SceneEntity scene) {
        SceneApplyResponseDTO response = new SceneApplyResponseDTO(scene.name());
        logger.log(Level.INFO, "applying scene {0}-{1}", new Object[]{ scene.id(), scene.name()});

        for (Entry<YeelightDeviceEntity, LightConfig> lightEntry : scene.lights().entrySet()) {
            YeelightDeviceEntity device = lightEntry.getKey();
            try {
                yeelightDeviceService.applyConfigToLight(device, lightEntry.getValue());
                response.addResponse(device.name(), null);
            } catch (AppException e) {
                logger.log(Level.INFO, "failed to apply scene to light: {0}-{1} -> {2}",
                    new Object[]{ device.name(), device.ip(), e.getMessage()});
                response.addResponse(device.name(), e.getErrorMessage());
            }
        }

        if(scene.spotifyResource() != null && !scene.spotifyResource().isEmpty()) {
            try {
                spotifyService.startContext(new SpotifyContextDTO(scene.spotifyResource(), scene.spotifyVolume()));
                response.addResponse("spotify", null);
            } catch (AppException e) {
                logger.log(Level.INFO, "failed to start spotify context -> {0}", e.getMessage());
                response.addResponse("spotify", e.getErrorMessage());
            }
        }

        if(response.isFailed()) logger.log(Level.INFO, "applied scene with failures");
        else logger.log(Level.INFO, "successfully applied scene");
        return response;
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
