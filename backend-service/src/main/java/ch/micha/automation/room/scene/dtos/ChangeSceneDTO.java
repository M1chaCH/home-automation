package ch.micha.automation.room.scene.dtos;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ChangeSceneDTO {
    private int id;
    private String name;
    private boolean defaultScene;
    private String spotifyResource;
    private int spotifyVolume;
    private List<SceneLightConfigDTO> lights;
}
