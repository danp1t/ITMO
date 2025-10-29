package com.danp1t.rest;

import com.danp1t.bean.Address;
import com.danp1t.bean.Coordinates;
import com.danp1t.bean.Location;
import com.danp1t.bean.Organization;
import com.danp1t.service.AddressService;
import com.danp1t.service.CoordinatesService;
import com.danp1t.service.LocationService;
import com.danp1t.service.OrganizationService;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.json.JsonObject;
import jakarta.json.Json;
import com.danp1t.websocket.OrganizationsWebSocket;

@Path("/create")
@Produces(MediaType.APPLICATION_JSON)
public class CreateEntities {

    @Inject
    private CoordinatesService coordinatesService;

    @Inject
    private LocationService locationService;

    @Inject
    private AddressService addressService;

    @Inject
    private OrganizationService organizationService;

    @POST
    @Path("/coordinates")
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

    @POST
    @Path("/address")

    public Response createAddress(Address address) {
        try {
            Address savedAddress = addressService.createAddress(address);

            String jsonResponse = String.format(
                    "{\"id\": %d, \"street\": \"%s\", \"zipCode\": \"%s\"}",
                    savedAddress.getId(),
                    savedAddress.getStreet(),
                    savedAddress.getZipCode()
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
    @Path("/organization")
    public Response createOrganization(Organization organization) {
        try {
            Organization savedOrganization = organizationService.createOrganization(organization);

            JsonObject jsonResponse = Json.createObjectBuilder()
                    .add("id", savedOrganization.getId())
                    .add("name", savedOrganization.getName())
                    .add("annualTurnover", savedOrganization.getAnnualTurnover())
                    .add("employeesCount", savedOrganization.getEmployeesCount())
                    .add("rating", savedOrganization.getRating())
                    .add("type", savedOrganization.getType().toString())
                    .build();

            OrganizationsWebSocket.notifyTableUpdate();
            return Response.ok(jsonResponse.toString()).build();

        } catch (Exception e) {
            JsonObject errorResponse = Json.createObjectBuilder()
                    .add("error", e.getMessage())
                    .build();

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorResponse.toString())
                    .build();
        }
    }
}
