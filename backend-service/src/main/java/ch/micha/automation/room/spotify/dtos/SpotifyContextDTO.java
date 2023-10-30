package ch.micha.automation.room.spotify.dtos;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class SpotifyContextDTO {
    private String contextUri;
    private String offsetUri = "";
    private int volume;

    public SpotifyContextDTO(String context, int volume) {
        this.contextUri = context;
        this.volume = volume;
    }
}
