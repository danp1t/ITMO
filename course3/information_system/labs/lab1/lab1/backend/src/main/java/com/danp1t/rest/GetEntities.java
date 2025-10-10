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
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/get")
@Produces(MediaType.APPLICATION_JSON)
public class GetEntities {

    @Inject
    private AddressService addressService;

    @Inject
    private CoordinatesService coordinatesService;

    @Inject
    private LocationService locationService;

    @Inject
    private OrganizationService organizationService;

    @GET
    @Path("/address")
    public Response getAddresses() {
        try {
            List<Address> addresses = addressService.getAllAddresses();

            if (addresses == null || addresses.isEmpty()) {
                JsonArray emptyArray = Json.createArrayBuilder().build();
                return Response.ok(emptyArray).build();
            }

            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            for (Address address : addresses) {
                arrayBuilder.add(Json.createObjectBuilder()
                        .add("id", address.getId())
                        .add("street", address.getStreet() != null ? address.getStreet() : "")
                        .add("zipCode", address.getZipCode() != null ? address.getZipCode() : "")
                        .add("displayName",
                                (address.getStreet() != null ? address.getStreet() : "") +
                                        ", " +
                                        (address.getZipCode() != null ? address.getZipCode() : ""))
                        .build());
            }

            JsonArray jsonArray = arrayBuilder.build();

            return Response.ok(jsonArray).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Ошибка при получении адресов: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @GET
    @Path("/coordinates")
    public Response getCoordinates() {
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

            JsonArray jsonArray = arrayBuilder.build();

            return Response.ok(jsonArray).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Ошибка при получении координат: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @GET
    @Path("/location")
    public Response getLocations() {
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

            JsonArray jsonArray = arrayBuilder.build();

            return Response.ok(jsonArray).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Ошибка при получении локаций: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    // Добавьте в класс GetEntities
    @GET
    @Path("/organization")
    public Response getOrganizations() {
        try {
            List<Organization> organizations = organizationService.getAllOrganizations();

            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            for (Organization org : organizations) {
                arrayBuilder.add(Json.createObjectBuilder()
                        .add("id", org.getId())
                        .add("name", org.getName())
                        .add("coordinates", org.getCoordinates().getId())
                        .add("officialAddress", org.getOfficialAddress().getId())
                        .add("postalAddress", org.getPostalAddress().getId())
                        .add("annualTurnover", org.getAnnualTurnover())
                        .add("employeesCount", org.getEmployeesCount())
                        .add("rating", org.getRating())
                        .add("type", org.getType().toString())
                        .add("creationDate", org.getCreationDate().toString())
                        .add("displayName", org.getName() + " (" + org.getType() + ")")
                        .build());
            }

            JsonArray jsonArray = arrayBuilder.build();
            return Response.ok(jsonArray).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Ошибка при получении организаций: " + e.getMessage() + "\"}")
                    .build();
        }
    }
}