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
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/get")
@Produces(MediaType.APPLICATION_JSON)
public class GetEntity {

    @Inject
    private AddressService addressService;

    @Inject
    private CoordinatesService coordinatesService;

    @Inject
    private LocationService locationService;

    @Inject
    private OrganizationService organizationService;

    @GET
    @Path("/address/{id}")
    public Response getAddresses(@PathParam("id") Long id) {
        try {
            Address address = addressService.getAddressById(id);

            if (address == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Address not found with id: " + id)
                        .build();
            }

            return Response.ok(address).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Ошибка при получении адреса: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @GET
    @Path("/coordinates/{id}")
    public Response getCoordinates(@PathParam("id") Long id) {
        try {
            Coordinates coordinates = coordinatesService.getCoordinatesById(id);

            if (coordinates == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Coordinates not found with id: " + id)
                        .build();
            }

            return Response.ok(coordinates).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Ошибка при получении координат: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @GET
    @Path("/location/{id}")
    public Response getLocation(@PathParam("id") Long id) {
        try {
            Location location = locationService.getLocationById(id);

            if (location == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Location not found with id: " + id)
                        .build();
            }

            return Response.ok(location).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Ошибка при получении локации: " + e.getMessage() + "\"}")
                    .build();
        }
    }
}