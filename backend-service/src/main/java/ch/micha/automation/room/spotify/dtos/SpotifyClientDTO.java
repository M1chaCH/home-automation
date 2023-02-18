package ch.micha.automation.room.spotify.dtos;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class SpotifyClientDTO {
    private String clientId;
    private String clientSecret;
}
