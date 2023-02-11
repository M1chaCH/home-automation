package ch.micha.automation.room.scene;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/automation/scene")
@RequestScoped()
public class SceneResource {
    private final SceneService service;

    @Inject
    public SceneResource(SceneService service) {
        this.service = service;
    }

    @POST()
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response applyScene(ApplySceneDTO dto) {
        service.applyScene(dto.getSceneId());

        return Response
                .status(Response.Status.NO_CONTENT)
                .build();
    }
}
