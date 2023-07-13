package ch.micha.automation.room.alarm.dtos;

import ch.micha.automation.room.spotify.dtos.SpotifyResourceDTO;
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
    private SpotifyResourceDTO audio;
    private int maxVolume;
}
