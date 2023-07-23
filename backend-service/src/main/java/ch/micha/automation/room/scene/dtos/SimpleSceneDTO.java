package ch.micha.automation.room.scene.dtos;

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
public class SimpleSceneDTO {
    private int id;
    private String name;
    private boolean defaultScene;
    private String spotifyResourceName;
    private String spotifyResourceImage;
    private int spotifyVolume;
    private int deviceCount;
}
