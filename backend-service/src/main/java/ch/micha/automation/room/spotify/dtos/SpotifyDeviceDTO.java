package ch.micha.automation.room.spotify.dtos;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class SpotifyDeviceDTO {
    private String id;
    private boolean active;
    private boolean privateSession;
    private boolean restricted;
    private String name;
    private String type;
    private int volume;
}
