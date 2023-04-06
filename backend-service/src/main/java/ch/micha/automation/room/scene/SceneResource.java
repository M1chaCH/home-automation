package ch.micha.automation.room.scene;

import ch.micha.automation.room.events.Logged;
import ch.micha.automation.room.scene.dtos.ApplySceneDTO;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Logged
@Path("/automation/scene")
@RequestScoped()
public class SceneResource {
    private final SceneService service;
    private final SceneCrudService crudService;

    @Inject
    public SceneResource(SceneService service, SceneCrudService crudService) {
        this.service = service;
        this.crudService = crudService;
    }

    @GET
    @Path("/crud")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getScenes() {
        return Response.status(Response.Status.OK).entity(crudService.loadScenes()).build();
    }

    @GET
    @Path("/crud/{sceneId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getScene(@PathParam("sceneId") int sceneId) {
        return Response.status(Response.Status.NOT_IMPLEMENTED).entity("method has not been implemented yet ):").build();
    }

    @POST
    @Path("/crud")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createScene() {
        return Response.status(Response.Status.NOT_IMPLEMENTED).entity("method has not been implemented yet ):").build();
    }

    @PUT
    @Path("/crud")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateScene() {
        return Response.status(Response.Status.NOT_IMPLEMENTED).entity("method has not been implemented yet ):").build();
    }

    @DELETE
    @Path("/crud/{sceneId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteScene(@PathParam("sceneId") int sceneId) {
        return Response.status(Response.Status.NOT_IMPLEMENTED).entity("method has not been implemented yet ):").build();
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
