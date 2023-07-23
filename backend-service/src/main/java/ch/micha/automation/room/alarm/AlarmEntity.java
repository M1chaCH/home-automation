package ch.micha.automation.room.alarm;

import ch.micha.automation.room.alarm.dtos.AlarmDTO;
import ch.micha.automation.room.errorhandling.exceptions.ResourceNotFoundException;
import ch.micha.automation.room.scene.SceneEntity;
import java.util.List;

public record AlarmEntity(
    int id,
    String cronSchedule,
    boolean active,
    int sceneId
) {

    public AlarmDTO asDTO(List<SceneEntity> sceneNames){
        SceneEntity sceneName = sceneNames.stream()
            .filter(scene -> scene.id() == this.sceneId())
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("scene for alarm", String.valueOf(this.sceneId())));

        String formattedTime = createFormattedTime();
        byte[] days = createDaysArray();

        return new AlarmDTO(
            this.id(),
            formattedTime,
            days,
            this.active(),
            sceneName.id(),
            sceneName.name()
        );
    }

    public AlarmDTO asDTO(int sceneId, String sceneName){
        String formattedTime = createFormattedTime();
        byte[] days = createDaysArray();

        return new AlarmDTO(
            this.id(),
            formattedTime,
            days,
            this.active(),
            sceneId,
            sceneName
        );
    }

    private String createFormattedTime() {
        String[] scheduleParts = this.cronSchedule().split("\\s+");
        return String.format("%02d:%02d", Integer.parseInt(scheduleParts[1]), Integer.parseInt(scheduleParts[0]));
    }

    private byte[] createDaysArray() {
        String[] dayParts = this.cronSchedule().split("\\s+")[4].split(",");
        byte[] days = new byte[dayParts.length];
        for (int i = 0; i < dayParts.length; i++)
            days[i] = Byte.parseByte(dayParts[i].trim());
        return days;
    }
}
