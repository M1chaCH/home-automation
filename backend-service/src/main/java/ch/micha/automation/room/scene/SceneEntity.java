package ch.micha.automation.room.scene;

import ch.micha.automation.room.light.LightConfigDTO;
import ch.micha.automation.room.light.YeelightDeviceEntity;

import java.util.Map;

public record SceneEntity(
        int id,
        String name,
        boolean defaultScene,
        Map<YeelightDeviceEntity, LightConfigDTO> lights
) {
    public SceneEntity(int id, SceneEntity entity) {
        this(id, entity.name(), entity.defaultScene(), entity.lights());
    }
}
