package ch.micha.automation.room.spotify.dtos;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class SpotifyPlayerDTO {
    private String songName;
    private String artistName;
    private String albumName;
    private String albumCoverUrl;
    private String deviceName;
    private String trackUrl;
    private boolean playing;
}
