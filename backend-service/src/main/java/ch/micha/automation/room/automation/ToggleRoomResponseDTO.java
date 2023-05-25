package ch.micha.automation.room.automation;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ToggleRoomResponseDTO {
    private boolean on;
    private boolean success;
}
