package ch.micha.automation.room.alarm;

import ch.micha.automation.room.alarm.dtos.AlarmDTO;
import ch.micha.automation.room.errorhandling.exceptions.ResourceNotFoundException;
import ch.micha.automation.room.spotify.SpotifyService;
import ch.micha.automation.room.spotify.dtos.SpotifyResourceDTO;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;
import java.util.StringJoiner;

@ApplicationScoped
public class AlarmService {
    private final AlarmProvider provider;
    private final SpotifyService spotifyService;

    @Inject
    public AlarmService(AlarmProvider provider, SpotifyService spotifyService) {
        this.provider = provider;
        this.spotifyService = spotifyService;
    }

    public List<AlarmDTO> loadAlarms() {
        List<AlarmEntity> entities = provider.loadAlarms();
        List<SpotifyResourceDTO> spotifyResources = spotifyService.loadResources();

        return entities.stream()
            .map(entity -> parseEntityToDto(entity, spotifyResources))
            .toList();
    }

    public AlarmDTO createAlarm(AlarmDTO toCreate) {
        List<SpotifyResourceDTO> spotifyResources = spotifyService.loadResources();
        AlarmEntity toCreateEntity = parseDtoToEntity(toCreate);
        AlarmEntity createdAlarm = provider.createAlarm(
            toCreateEntity.cronSchedule(),
            toCreateEntity.spotifyResource(),
            toCreateEntity.maxVolume()
        );

        return parseEntityToDto(createdAlarm, spotifyResources);
    }

    public void updateAlarm(AlarmDTO toUpdate) {
        AlarmEntity toUpdateEntity = parseDtoToEntity(toUpdate);
        provider.updateAlarm(
            toUpdateEntity.id(),
            toUpdateEntity.cronSchedule(),
            toUpdateEntity.active(),
            toUpdateEntity.spotifyResource(),
            toUpdateEntity.maxVolume()
        );
    }

    public void deleteAlarm(int id) {
        provider.deleteAlarm(id);
    }

    private AlarmDTO parseEntityToDto(AlarmEntity entity, List<SpotifyResourceDTO> spotifyResources){
        SpotifyResourceDTO spotifyResource = spotifyResources.stream()
            .filter(resource -> resource.getSpotifyURI().equals(entity.spotifyResource()))
            .findAny()
            .orElseThrow(() -> new ResourceNotFoundException("alarm audio", entity.spotifyResource()));

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
            spotifyResource,
            entity.maxVolume()
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
            dto.getAudio().getSpotifyURI(),
            dto.getMaxVolume()
        );
    }
}
