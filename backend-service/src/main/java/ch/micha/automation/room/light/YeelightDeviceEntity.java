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
    /** primary key, should match with YeeLight location */
    private String id;
    private String name;
    private YeeLight light;

    public boolean isOnline() {
        return light != null;
    }

    @Override
    public String toString() {
        return "YeelightDeviceEntity{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
