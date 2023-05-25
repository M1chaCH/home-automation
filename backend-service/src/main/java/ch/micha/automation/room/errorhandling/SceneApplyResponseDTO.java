package ch.micha.automation.room.errorhandling;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SceneApplyResponseDTO {
    private final String name;
    private final List<ApplyResponseDTO> responses = new ArrayList<>();
    private boolean failed = false;

    public SceneApplyResponseDTO(String name) {
        this.name = name;
    }

    public void addResponse(String identifier, ErrorMessageDTO failCause) {
        responses.add(new ApplyResponseDTO(
            identifier,
            failCause != null,
            failCause
        ));
        failed = failCause != null || failed;
    }
}
