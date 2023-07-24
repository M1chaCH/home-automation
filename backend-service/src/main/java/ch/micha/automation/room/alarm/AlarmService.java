package ch.micha.automation.room.alarm;

import ch.micha.automation.room.alarm.dtos.AlarmDTO;
import ch.micha.automation.room.scene.SceneEntity;
import ch.micha.automation.room.scene.SceneProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;

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
            .map(entity -> entity.asDTO(sceneNames))
            .toList();
    }

    public AlarmDTO loadNextAlarmAsDto() {
        Optional<AlarmEntity> entity = alarmTrigger.loadNextAlarm();
        return entity.map(alarmEntity -> alarmEntity.asDTO(sceneProvider.loadSimpleScenes()))
            .orElse(null);
    }

    public AlarmDTO createAlarm(AlarmDTO toCreate) {
        List<SceneEntity> sceneNames = sceneProvider.loadSimpleScenes();
        AlarmEntity toCreateEntity = toCreate.asEntity();
        AlarmEntity createdAlarm = provider.createAlarm(
            toCreateEntity.cronSchedule(),
            toCreateEntity.sceneId()
        );

        return createdAlarm.asDTO(sceneNames);
    }

    public void updateAlarm(AlarmDTO toUpdate) {
        AlarmEntity toUpdateEntity = toUpdate.asEntity();
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
}
