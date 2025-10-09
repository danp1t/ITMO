package com.danp1t.rest;

import com.danp1t.bean.Coordinates;
import com.danp1t.service.CoordinatesService;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/create/coordinates")
public class CreateEntities {

    @Inject
    private CoordinatesService coordinatesService;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response createCoordinates(Coordinates coordinates) {
        try {
            Coordinates savedCoordinates = coordinatesService.createCoordinates(coordinates);

            // Создаем JSON ответ
            String jsonResponse = String.format(
                    "{\"id\": %d, \"x\": %s, \"y\": %s}",
                    savedCoordinates.getId(),
                    savedCoordinates.getX(),
                    savedCoordinates.getY()
            );

            return Response.ok(jsonResponse).build();

        } catch (Exception e) {
            // Обработка ошибок валидации
            String errorResponse = String.format(
                    "{\"error\": \"%s\"}",
                    e.getMessage()
            );
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorResponse)
                    .build();
        }
    }
}