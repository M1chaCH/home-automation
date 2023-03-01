package ch.micha.automation.room.light.yeelight.dtos;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RenameYeelightDeviceDTO {
    private String oldName;
    private String newName;
}
