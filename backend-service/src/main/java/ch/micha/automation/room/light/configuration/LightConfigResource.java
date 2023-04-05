package ch.micha.automation.room.light.configuration;

import ch.micha.automation.room.events.Logged;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Logged
@Path("/automation/config/rest")
@RequestScoped
public class LightConfigResource {

    private final LightConfigService service;

    @Inject
    public LightConfigResource(LightConfigService service) {
        this.service = service;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLightConfigs() {
        return Response.status(Response.Status.OK).entity(service.loadLightConfigs()).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createLightConfig(LightConfig config) {
        return Response.status(Response.Status.OK).entity(service.createConfig(config)).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateLightConfig(LightConfig config) {
        service.updateConfig(config);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @DELETE
    @Path("{id}")
    public Response deleteLightConfig(@PathParam("id") int id) {
        service.deleteConfig(id);
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
