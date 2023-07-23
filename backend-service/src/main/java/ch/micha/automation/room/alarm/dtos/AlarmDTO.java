package ch.micha.automation.room.alarm.dtos;

import ch.micha.automation.room.alarm.AlarmEntity;
import java.util.StringJoiner;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AlarmDTO {
    private int id;
    private String time;
    private byte[] days;
    private boolean active;
    private int sceneId;
    private String sceneName;

    public AlarmEntity asEntity() {
        String hour = time.split(":")[0];
        String minute = time.split(":")[1];

        StringJoiner dayJoiner = new StringJoiner(",");
        for (byte day : days) dayJoiner.add(String.valueOf(day));

        return new AlarmEntity(
            id,
            String.format("%s %s * * %s", minute, hour, dayJoiner),
            active,
            sceneId
        );
    }
}
