package com.danp1t.rest;

import com.danp1t.bean.*;
import com.danp1t.service.AddressService;
import com.danp1t.service.CoordinatesService;
import com.danp1t.service.LocationService;
import com.danp1t.service.OrganizationService;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

@Path("/update")
public class UpdateEntity {

    @Inject
    private AddressService addressService;

    @Inject
    private CoordinatesService coordinatesService;

    @Inject
    private LocationService locationService;

    @Inject
    private OrganizationService organizationService;

    @PUT
    @Path("/coordinates/{id}")
    public Response updateCoordinates(@PathParam("id") Long id, Coordinates coordinates) {
        coordinates.setId(id);
        try {
            if (!id.equals(coordinates.getId())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("ID in path does not match ID in request body")
                        .build();
            }

            Coordinates updatedCoordinates = coordinatesService.updateCoordinates(coordinates);

            if (updatedCoordinates == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Coordinates not found with id: " + id)
                        .build();
            }

            return Response.ok(updatedCoordinates).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Ошибка при обновлении координат: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @PUT
    @Path("/address/{id}")
    public Response updateAddress(@PathParam("id") Long id, Address address) {
        address.setId(id);
        try {
            if (!id.equals(address.getId())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("ID in path does not match ID in request body")
                        .build();
            }

            Address updatedAddress = addressService.updateAddress(address);

            if (updatedAddress == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Address not found with id: " + id)
                        .build();
            }

            return Response.ok(updatedAddress).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Ошибка при обновлении адреса: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @PUT
    @Path("/location/{id}")
    public Response updateLocation(@PathParam("id") Long id, Location location) {
        location.setId(id);
        try {
            if (!id.equals(location.getId())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("ID in path does not match ID in request body")
                        .build();
            }

            Location updatedLocation = locationService.updateLocation(location);

            if (updatedLocation == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Location not found with id: " + id)
                        .build();
            }

            return Response.ok(updatedLocation).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Ошибка при обновлении локации: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @PUT
    @Path("/organization/{id}")
    public Response updateOrganization(@PathParam("id") Long id, @Valid OrganizationDTO organizationUpdateDto) {
        try {
            Organization updatedOrganization = organizationService.updateOrganization(id, organizationUpdateDto);

            if (updatedOrganization == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Organization not found with id: " + id)
                        .build();
            }

            return Response.ok(updatedOrganization).build();

        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\": \"Ошибка валидации: " + e.getMessage() + "\"}")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Ошибка при обновлении организации: " + e.getMessage() + "\"}")
                    .build();
        }
    }
}