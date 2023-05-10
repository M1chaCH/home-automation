package ch.micha.automation.room.scene;

import ch.micha.automation.room.light.configuration.LightConfig;
import ch.micha.automation.room.light.yeelight.YeelightDeviceEntity;
import ch.micha.automation.room.light.yeelight.YeelightDeviceProvider;
import ch.micha.automation.room.scene.dtos.ChangeSceneDTO;
import ch.micha.automation.room.scene.dtos.SceneDTO;
import ch.micha.automation.room.scene.dtos.SceneLightConfigDTO;
import ch.micha.automation.room.spotify.SpotifyApiWrapper;
import ch.micha.automation.room.spotify.dtos.SpotifyResourceDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class SceneCrudService {
    private final SceneProvider provider;
    private final YeelightDeviceProvider deviceProvider;
    private final SpotifyApiWrapper spotifyApi;

    @Inject
    public SceneCrudService(SceneProvider provider, YeelightDeviceProvider deviceProvider, SpotifyApiWrapper spotifyApi) {
        this.provider = provider;
        this.deviceProvider = deviceProvider;
        this.spotifyApi = spotifyApi;
    }

    public List<SceneDTO> loadScenes() {
        return provider.loadScenes().stream().map(this::parseSceneDTO).toList();
    }

    public SceneDTO createScene(ChangeSceneDTO toCreate) {
        SceneEntity createdScene = provider.createNewScene(toCreate.getName(), false, toCreate.getSpotifyResource(),
                toCreate.getSpotifyVolume(), parseSceneLightConfig(toCreate.getLights()));
        return parseSceneDTO(createdScene);
    }

    public void updateScene(ChangeSceneDTO toUpdate) {
        provider.updateScene(toUpdate.getId(), toUpdate.getName(), toUpdate.getSpotifyResource(),
                toUpdate.getSpotifyVolume(), parseSceneLightConfig(toUpdate.getLights()));
    }

    public void deleteScene(int id) {
        provider.deleteScene(id);
    }

    public SceneDTO parseSceneDTO(SceneEntity entity) {
        SpotifyResourceDTO resource = spotifyApi.getSavedSpotifyResource(entity.spotifyResource())
                .orElse(null);
        return entity.toDto(resource);
    }

    private Map<YeelightDeviceEntity, LightConfig> parseSceneLightConfig(List<SceneLightConfigDTO> sceneLightConfigDTO) {
        return sceneLightConfigDTO.stream().collect(Collectors.toMap(
                dto -> deviceProvider.findFromDto(dto.getDevice()),
                SceneLightConfigDTO::getLightConfig
        ));
    }
}
