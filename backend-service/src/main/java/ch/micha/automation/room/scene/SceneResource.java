package ch.micha.automation.room.scene;

import ch.micha.automation.room.events.Logged;
import ch.micha.automation.room.scene.dtos.ChangeSceneDTO;
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

    @POST
    @Path("/crud")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createScene(ChangeSceneDTO scene) {
        return Response.status(Response.Status.CREATED).entity(crudService.createScene(scene)).build();
    }

    @PUT
    @Path("/crud")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateScene(ChangeSceneDTO scene) {
        crudService.updateScene(scene);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @DELETE
    @Path("/crud/{sceneId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteScene(@PathParam("sceneId") int sceneId) {
        crudService.deleteScene(sceneId);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @POST()
    @Path("/{sceneId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response applyScene(@PathParam("sceneId") int sceneId) {
        return Response
            .status(Response.Status.OK)
            .entity(service.applyScene(sceneId))
            .build();
    }
}
