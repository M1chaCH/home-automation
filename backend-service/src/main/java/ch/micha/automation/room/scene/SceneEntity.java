package ch.micha.automation.room.scene;

import ch.micha.automation.room.light.configuration.LightConfigEntity;
import ch.micha.automation.room.light.yeelight.YeelightDeviceEntity;

import java.util.Map;

public record SceneEntity(
        int id,
        String name,
        boolean defaultScene,
        Map<YeelightDeviceEntity, LightConfigEntity> lights
) { }
