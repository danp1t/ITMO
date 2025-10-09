package com.danp1t.rest;

import com.danp1t.bean.Coordinates;
import com.danp1t.bean.Location;
import com.danp1t.service.CoordinatesService;
import com.danp1t.service.LocationService;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/create")
public class CreateEntities {

    @Inject
    private CoordinatesService coordinatesService;

    @Inject
    private LocationService locationService;

    @POST
    @Path("/coordinates")
    @Produces(MediaType.APPLICATION_JSON)
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

    @POST
    @Path("/location")
    @Produces(MediaType.APPLICATION_JSON)
    public Response createLocation(Location location) {
        try {
            Location savedLocation = locationService.createLocation(location);

            String jsonResponse = String.format(
                    "{\"id\": %d, \"x\": %s, \"y\": %s, \"z\": %s, \"name\": %s,}",
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
}