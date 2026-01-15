package com.danp1t.controller;

import com.danp1t.model.Coordinates;
import com.danp1t.service.CoordinatesService;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/coordinates")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CoordinatesController {

    @Inject
    private CoordinatesService coordinatesService;

    @GET
    public Response getAllCoordinates() {
        try {
            List<Coordinates> coordinatesList = coordinatesService.getAllCoordinates();

            if (coordinatesList == null || coordinatesList.isEmpty()) {
                JsonArray emptyArray = Json.createArrayBuilder().build();
                return Response.ok(emptyArray).build();
            }

            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            for (Coordinates coord : coordinatesList) {
                arrayBuilder.add(Json.createObjectBuilder()
                        .add("id", coord.getId())
                        .add("x", coord.getX() != null ? coord.getX() : 0)
                        .add("y", coord.getY())
                        .add("displayName", "X: " + (coord.getX() != null ? coord.getX() : 0) + ", Y: " + coord.getY())
                        .build());
            }

            return Response.ok(arrayBuilder.build()).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Ошибка при получении координат: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    public Response getCoordinatesById(@PathParam("id") Long id) {
        try {
            Coordinates coordinates = coordinatesService.getCoordinatesById(id);

            if (coordinates == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Координаты не найдены по ID: " + id)
                        .build();
            }

            return Response.ok(coordinates).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Ошибка при получении координат: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @POST
    public Response createCoordinates(Coordinates coordinates) {
        try {
            Coordinates savedCoordinates = coordinatesService.createCoordinates(coordinates);

            String jsonResponse = String.format(
                    "{\"id\": %d, \"x\": %s, \"y\": %s}",
                    savedCoordinates.getId(),
                    savedCoordinates.getX(),
                    savedCoordinates.getY()
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
    public Response updateCoordinates(@PathParam("id") Long id, Coordinates coordinates) {
        coordinates.setId(id);
        try {
            if (!id.equals(coordinates.getId())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("ID в Path не совпадает с ID Body")
                        .build();
            }

            Coordinates updatedCoordinates = coordinatesService.updateCoordinates(coordinates);

            if (updatedCoordinates == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Координаты не найдены по ID: " + id)
                        .build();
            }

            return Response.ok(updatedCoordinates).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Ошибка при обновлении координат: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteCoordinates(@PathParam("id") Long id) {
        try {
            boolean deleted = coordinatesService.deleteCoordinates(id);

            if (!deleted) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Координаты не найдены по ID: " + id)
                        .build();
            }

            return Response.ok()
                    .entity("{\"message\": \"Координаты успешно удалены\"}")
                    .build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Ошибка при удалении координат: " + e.getMessage() + "\"}")
                    .build();
        }
    }
}