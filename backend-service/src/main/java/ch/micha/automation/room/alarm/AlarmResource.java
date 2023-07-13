package ch.micha.automation.room.alarm;

import ch.micha.automation.room.alarm.dtos.AlarmDTO;
import ch.micha.automation.room.events.Logged;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Logged
@Path("/automation/alarm")
@RequestScoped()
public class AlarmResource {

    private final AlarmService service;

    @Inject
    public AlarmResource(AlarmService service) {
        this.service = service;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAlarms() {
        return Response.status(Status.OK).entity(service.loadAlarms()).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createAlarm(AlarmDTO alarm) {
        return Response.status(Status.OK).entity(service.createAlarm(alarm)).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateAlarm(AlarmDTO alarm) {
        service.updateAlarm(alarm);
        return Response.status(Status.NO_CONTENT).build();
    }

    @DELETE
    @Path("{id}")
    public Response deleteAlarm(@PathParam("id") int id) {
        service.deleteAlarm(id);
        return Response.status(Status.NO_CONTENT).build();
    }
}
