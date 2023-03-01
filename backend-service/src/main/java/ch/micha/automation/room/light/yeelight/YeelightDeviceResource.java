package ch.micha.automation.room.light.yeelight;

import ch.micha.automation.room.events.Logged;
import ch.micha.automation.room.light.yeelight.dtos.RenameYeelightDeviceDTO;
import ch.micha.automation.room.light.yeelight.dtos.YeelightDeviceDTO;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Logged
@Path("/automation/device")
@RequestScoped
public class YeelightDeviceResource {
    private final YeelightDeviceService service;

    @Inject
    public YeelightDeviceResource(YeelightDeviceService service) {
        this.service = service;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStoredDevices() {
        return Response.status(Response.Status.OK).entity(service.getAllDevices()).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addDevice(YeelightDeviceDTO device) {
        return Response
                .status(Response.Status.OK)
                .entity(service.addNewDevice(device.getName(), device.getIp()))
                .build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response renameDevice(RenameYeelightDeviceDTO rename) {
        service.renameDevice(rename.getOldName(), rename.getNewName());
        return Response
                .status(Response.Status.NO_CONTENT)
                .build();
    }

    @PUT
    @Path("{name}")
    public Response toggleDevicePower(@PathParam("name") String name) {
        service.togglePower(name);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @DELETE
    @Path("/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response renameDevice(@PathParam("name") String name) {
        service.removeDevice(name);
        return Response
                .status(Response.Status.NO_CONTENT)
                .build();
    }
}
