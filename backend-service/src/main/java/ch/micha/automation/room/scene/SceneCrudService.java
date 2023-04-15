package ch.micha.automation.room.scene;

import ch.micha.automation.room.light.configuration.LightConfig;
import ch.micha.automation.room.light.yeelight.YeelightDeviceEntity;
import ch.micha.automation.room.light.yeelight.YeelightDeviceProvider;
import ch.micha.automation.room.scene.dtos.SceneDTO;
import ch.micha.automation.room.scene.dtos.SceneLightConfigDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class SceneCrudService {
    private final SceneProvider provider;
    private final YeelightDeviceProvider deviceProvider;

    @Inject
    public SceneCrudService(SceneProvider provider, YeelightDeviceProvider deviceProvider) {
        this.provider = provider;
        this.deviceProvider = deviceProvider;
    }

    public List<SceneDTO> loadScenes() {
        return provider.loadScenes().stream().map(SceneEntity::toDto).toList();
    }

    public SceneDTO createScene(SceneDTO toCreate) {
        return provider.createNewScene(toCreate.getName(), false, toCreate.getSpotifyResource(),
                toCreate.getSpotifyVolume(), parseSceneLightConfig(toCreate.getLights())).toDto();
    }

    public void updateScene(SceneDTO toUpdate) {
        provider.updateScene(toUpdate.getId(), toUpdate.getName(), toUpdate.getSpotifyResource(),
                toUpdate.getSpotifyVolume(), parseSceneLightConfig(toUpdate.getLights()));
    }

    public void deleteScene(int id) {
        provider.deleteScene(id);
    }

    private Map<YeelightDeviceEntity, LightConfig> parseSceneLightConfig(List<SceneLightConfigDTO> sceneLightConfigDTO) {
        return sceneLightConfigDTO.stream().collect(Collectors.toMap(
                dto -> deviceProvider.findFromDto(dto.getDevice()),
                SceneLightConfigDTO::getLightConfig
        ));
    }
}
