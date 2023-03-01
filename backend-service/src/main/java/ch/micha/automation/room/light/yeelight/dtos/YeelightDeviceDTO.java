package ch.micha.automation.room.light.yeelight.dtos;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class YeelightDeviceDTO {
    private String name;
    private String ip;
    private boolean online;
}
