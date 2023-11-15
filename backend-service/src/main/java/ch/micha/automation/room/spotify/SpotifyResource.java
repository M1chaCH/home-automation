package ch.micha.automation.room.spotify;

import ch.micha.automation.room.events.Logged;
import ch.micha.automation.room.spotify.dtos.SpotifyCodeDTO;
import ch.micha.automation.room.spotify.dtos.SpotifyContextDTO;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

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

    @GET
    @Path("/player")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSpotifyPlayerState() {
        return Response.status(Response.Status.OK).entity(service.loadCurrentPlayer()).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response storeConnection(SpotifyCodeDTO dto) {
        service.addAuthorisation(dto);

        return Response.status(Response.Status.CREATED).build();
    }

    @POST
    @Path("/playback")
    public Response startResource(SpotifyContextDTO context) {
        service.startContext(context);
        return Response.status(Status.NO_CONTENT).build();
    }

    @PUT
    @Path("/playback")
    public Response togglePlayback() {
        service.togglePlayback();
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @PUT
    @Path("/playback/next")
    @SuppressWarnings("java:S1130") // says it never throws interrupt exception -> is not true
    public Response skipToNextSong() throws InterruptedException {
        return Response.status(Response.Status.OK).entity(service.skipToNextSong()).build();
    }

    @PUT
    @Path("/playback/previous")
    @SuppressWarnings("java:S1130") // says it never throws interrupt exception -> is not true
    public Response previousSong() throws InterruptedException {
        return Response.status(Response.Status.OK).entity(service.previousSong()).build();
    }
}
