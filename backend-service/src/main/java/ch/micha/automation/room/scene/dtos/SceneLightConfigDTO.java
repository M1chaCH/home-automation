package ch.micha.automation.room.scene.dtos;

import ch.micha.automation.room.light.configuration.LightConfig;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SceneLightConfigDTO {
    private int deviceId;
    private String deviceName;

    private LightConfig lightConfig;
}
