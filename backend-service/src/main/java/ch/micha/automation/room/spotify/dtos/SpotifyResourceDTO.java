package ch.micha.automation.room.spotify.dtos;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class SpotifyResourceDTO  {
    private String name;
    private String description;
    private String spotifyURI;
    private String href;
    private String imageUrl;
}
