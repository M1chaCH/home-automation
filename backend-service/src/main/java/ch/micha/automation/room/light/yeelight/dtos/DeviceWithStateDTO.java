package ch.micha.automation.room.light.yeelight.dtos;

import ch.micha.automation.room.light.configuration.LightConfig;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class DeviceWithStateDTO {
    private String name;
    private String ip;
    private boolean online;
    private LightConfig state;
}
