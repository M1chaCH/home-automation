package ch.micha.automation.room.scene;

import ch.micha.automation.room.light.configuration.LightConfig;
import ch.micha.automation.room.light.yeelight.YeelightDeviceEntity;
import ch.micha.automation.room.scene.dtos.SceneDTO;
import ch.micha.automation.room.scene.dtos.SceneLightConfigDTO;

import java.util.Map;

public record SceneEntity(
        int id,
        String name,
        boolean defaultScene,
        Map<YeelightDeviceEntity, LightConfig> lights,
        String spotifyResource,
        int spotifyVolume
) {
    public SceneDTO toDto() {
        return new SceneDTO(id, name, defaultScene, spotifyResource, spotifyVolume, lights.entrySet()
                .stream()
                .map(entry -> new SceneLightConfigDTO(entry.getKey().toDto(), entry.getValue())).toList()
        );
    }
}