package ch.micha.automation.room.errorhandling;

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
public class ApplyResponseDTO {
    private String identifier;
    private boolean failed;
    private ErrorMessageDTO failCause;
}
