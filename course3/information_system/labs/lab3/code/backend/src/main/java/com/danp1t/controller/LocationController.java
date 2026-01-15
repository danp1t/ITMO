package com.danp1t.controller;

import com.danp1t.model.Location;
import com.danp1t.service.LocationService;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/location")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LocationController {

    @Inject
    private LocationService locationService;

    @GET
    public Response getAllLocations() {
        try {
            List<Location> locations = locationService.getAllLocations();

            if (locations == null || locations.isEmpty()) {
                JsonArray emptyArray = Json.createArrayBuilder().build();
                return Response.ok(emptyArray).build();
            }

            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            for (Location location : locations) {
                arrayBuilder.add(Json.createObjectBuilder()
                        .add("id", location.getId())
                        .add("x", location.getX() != null ? location.getX() : 0)
                        .add("y", location.getY() != null ? location.getY() : 0)
                        .add("z", location.getZ())
                        .add("name", location.getName() != null ? location.getName() : "")
                        .add("displayName", location.getName() != null ? location.getName() : "")
                        .build());
            }

            return Response.ok(arrayBuilder.build()).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Ошибка при получении локаций: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    public Response getLocationById(@PathParam("id") Long id) {
        try {
            Location location = locationService.getLocationById(id);

            if (location == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Локация не найдена по ID: " + id)
                        .build();
            }

            return Response.ok(location).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Ошибка при получении локации: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @POST
    public Response createLocation(Location location) {
        try {
            Location savedLocation = locationService.createLocation(location);

            String jsonResponse = String.format(
                    "{\"id\": %d, \"x\": \"%s\", \"y\": \"%s\", \"z\": \"%s\", \"name\": \"%s\"}",
                    savedLocation.getId(),
                    savedLocation.getX(),
                    savedLocation.getY(),
                    savedLocation.getZ(),
                    savedLocation.getName()
            );

            return Response.ok(jsonResponse).build();

        } catch (Exception e) {
            String errorResponse = String.format(
                    "{\"error\": \"%s\"}",
                    e.getMessage()
            );
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorResponse)
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response updateLocation(@PathParam("id") Long id, Location location) {
        location.setId(id);
        try {
            if (!id.equals(location.getId())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("ID в Path не совпадает с ID Body")
                        .build();
            }

            Location updatedLocation = locationService.updateLocation(location);

            if (updatedLocation == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Локация не найдена по ID: " + id)
                        .build();
            }

            return Response.ok(updatedLocation).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Ошибка при обновлении локации: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteLocation(@PathParam("id") Long id) {
        try {
            boolean deleted = locationService.deleteLocation(id);

            if (!deleted) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Локация не найдена по ID: " + id)
                        .build();
            }

            return Response.ok()
                    .entity("{\"message\": \"Локация успешно удалена\"}")
                    .build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Ошибка при удалении локации: " + e.getMessage() + "\"}")
                    .build();
        }
    }
}