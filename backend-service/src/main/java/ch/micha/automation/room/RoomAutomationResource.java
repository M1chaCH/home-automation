package ch.micha.automation.room;

import ch.micha.automation.room.scene.ApplySceneDTO;
import ch.micha.automation.room.scene.SceneProvider;
import ch.micha.automation.room.scene.presets.Scene;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/automation")
@RequestScoped()
public class RoomAutomationResource {

    private final SceneProvider sceneProvider;

    @Inject
    public RoomAutomationResource(SceneProvider sceneProvider) {
        this.sceneProvider = sceneProvider;
    }

    @PUT()
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response applyScene(ApplySceneDTO dto) {
        Scene scene = sceneProvider.findSceneById(dto.getSceneId());

        if(scene != null) {
            scene.apply();
            return Response
                    .status(Response.Status.NO_CONTENT)
                    .build();
        }

        return Response
                .status(Response.Status.NOT_FOUND)
                .entity(new MessageDTO(dto.getSceneId() + " not found"))
                .build();
    }
}
