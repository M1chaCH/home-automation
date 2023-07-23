package ch.micha.automation.room.alarm;

import ch.micha.automation.room.alarm.dtos.AlarmDTO;
import ch.micha.automation.room.errorhandling.exceptions.ResourceNotFoundException;
import ch.micha.automation.room.scene.SceneEntity;
import ch.micha.automation.room.scene.SceneProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.StringJoiner;

@ApplicationScoped
public class AlarmService {
    private final AlarmProvider provider;
    private final AlarmTrigger alarmTrigger;
    private final SceneProvider sceneProvider;

    @Inject
    public AlarmService(AlarmProvider provider, AlarmTrigger alarmTrigger, SceneProvider sceneProvider) {
        this.provider = provider;
        this.alarmTrigger = alarmTrigger;
        this.sceneProvider = sceneProvider;
    }

    public List<AlarmDTO> loadAlarms() {
        List<AlarmEntity> entities = provider.loadAlarms();
        List<SceneEntity> sceneNames = sceneProvider.loadSimpleScenes();

        return entities.stream()
            .map(entity -> parseEntityToDto(entity, sceneNames))
            .toList();
    }

    public AlarmDTO loadNextAlarmAsDto() {
        List<SceneEntity> sceneNames = sceneProvider.loadSimpleScenes();
        return parseEntityToDto(alarmTrigger.loadNextAlarm(), sceneNames);
    }

    public AlarmDTO createAlarm(AlarmDTO toCreate) {
        List<SceneEntity> sceneNames = sceneProvider.loadSimpleScenes();
        AlarmEntity toCreateEntity = parseDtoToEntity(toCreate);
        AlarmEntity createdAlarm = provider.createAlarm(
            toCreateEntity.cronSchedule(),
            toCreateEntity.sceneId()
        );

        return parseEntityToDto(createdAlarm, sceneNames);
    }

    public void updateAlarm(AlarmDTO toUpdate) {
        AlarmEntity toUpdateEntity = parseDtoToEntity(toUpdate);
        provider.updateAlarm(
            toUpdateEntity.id(),
            toUpdateEntity.cronSchedule(),
            toUpdateEntity.active(),
            toUpdateEntity.sceneId()
        );
    }

    public void deleteAlarm(int id) {
        provider.deleteAlarm(id);
    }

    public void continueSceneOfAlarm() {
        alarmTrigger.continueSceneOfAlarm();
    }

    public void stopCurrentAlarm() {
        alarmTrigger.stopCurrentAlarm();
    }

    private AlarmDTO parseEntityToDto(AlarmEntity entity, List<SceneEntity> sceneNames){
        SceneEntity sceneName = sceneNames.stream()
            .filter(scene -> scene.id() == entity.sceneId())
            .findFirst()
            .orElseThrow(() -> new ResourceNotFoundException("scene for alarm", String.valueOf(entity.sceneId())));

        String[] scheduleParts = entity.cronSchedule().split("\\s+");
        String formattedTime = String.format("%02d:%02d", Integer.parseInt(scheduleParts[1]), Integer.parseInt(scheduleParts[0]));

        String[] dayParts = entity.cronSchedule().split("\\s+")[4].split(",");
        byte[] days = new byte[dayParts.length];
        for (int i = 0; i < dayParts.length; i++)
            days[i] = Byte.parseByte(dayParts[i].trim());

        return new AlarmDTO(
            entity.id(),
            formattedTime,
            days,
            entity.active(),
            sceneName.id(),
            sceneName.name()
        );
    }

    private AlarmEntity parseDtoToEntity(AlarmDTO dto) {
        String hour = dto.getTime().split(":")[0];
        String minute = dto.getTime().split(":")[1];

        StringJoiner dayJoiner = new StringJoiner(",");
        byte[] days = dto.getDays();
        for (byte day : days) dayJoiner.add(String.valueOf(day));

        return new AlarmEntity(
            dto.getId(),
            String.format("%s %s * * %s", minute, hour, dayJoiner),
            dto.isActive(),
            dto.getSceneId()
        );
    }
}
