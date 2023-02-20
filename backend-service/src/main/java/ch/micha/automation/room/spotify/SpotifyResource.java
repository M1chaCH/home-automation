package ch.micha.automation.room.spotify;

import ch.micha.automation.room.events.Logged;
import ch.micha.automation.room.spotify.dtos.SpotifyCodeDTO;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Logged
@Path("/automation/spotify")
@RequestScoped
public class SpotifyResource {
    private final SpotifyService service;


    @Inject
    public SpotifyResource(SpotifyService service) {
        this.service = service;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response isConnected() {
        service.getAccess();
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @GET
    @Path("/client")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSpotifyClient() {
        return Response.status(Response.Status.OK).entity(service.getClient()).build();
    }

    @GET
    @Path("/resources")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSpotifyResources() {
        return Response.status(Response.Status.OK).entity(service.loadResources()).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response storeConnection(SpotifyCodeDTO dto) {
        service.addAuthorisation(dto);

        return Response.status(Response.Status.CREATED).build();
    }

    @PUT
    @Path("/playback")
    public Response togglePlayback() {
        service.togglePlayback();
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
