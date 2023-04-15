package ch.micha.automation.room.scene.dtos;

import ch.micha.automation.room.light.configuration.LightConfig;
import ch.micha.automation.room.light.yeelight.dtos.YeelightDeviceDTO;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class SceneLightConfigDTO {
    private YeelightDeviceDTO device;

    private LightConfig lightConfig;
}
