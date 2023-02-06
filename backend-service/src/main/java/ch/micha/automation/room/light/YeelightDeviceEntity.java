package ch.micha.automation.room.light;

import com.moppletop.yeelight.api.model.YeeLight;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class YeelightDeviceEntity {
    private int id;
    private String name;
    private String ipAddress;
    private boolean online = false;
    private YeeLight light;
}
