package com.danp1t.rest;

import com.danp1t.service.AddressService;
import com.danp1t.service.CoordinatesService;
import com.danp1t.service.LocationService;
import com.danp1t.service.OrganizationService;
import jakarta.inject.Inject;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

@Path("/delete")
public class DeleteEntity {

    @Inject
    private AddressService addressService;

    @Inject
    private CoordinatesService coordinatesService;

    @Inject
    private LocationService locationService;

    @Inject
    private OrganizationService organizationService;

    @DELETE
    @Path("/organization/{id}")
    public Response deleteOrganization(@PathParam("id") Integer id) {
        try {
            boolean deleted = organizationService.deleteOrganization(id);

            if (!deleted) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Organization not found with id: " + id)
                        .build();
            }

            return Response.ok()
                    .entity("{\"message\": \"Organization deleted successfully\"}")
                    .build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Ошибка при удалении организации: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @DELETE
    @Path("/coordinates/{id}")
    public Response deleteCoordinates(@PathParam("id") Long id) {
        try {
            boolean deleted = coordinatesService.deleteCoordinates(id);

            if (!deleted) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Coordinates not found with id: " + id)
                        .build();
            }

            return Response.ok()
                    .entity("{\"message\": \"Coordinates deleted successfully\"}")
                    .build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Ошибка при удалении координат: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @DELETE
    @Path("/address/{id}")
    public Response deleteAddress(@PathParam("id") Long id) {
        try {
            boolean deleted = addressService.deleteAddress(id);

            if (!deleted) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Address not found with id: " + id)
                        .build();
            }

            return Response.ok()
                    .entity("{\"message\": \"Address deleted successfully\"}")
                    .build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Ошибка при удалении адреса: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @DELETE
    @Path("/location/{id}")
    public Response deleteLocation(@PathParam("id") Long id) {
        try {
            boolean deleted = locationService.deleteLocation(id);

            if (!deleted) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Location not found with id: " + id)
                        .build();
            }

            return Response.ok()
                    .entity("{\"message\": \"Location deleted successfully\"}")
                    .build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Ошибка при удалении локации: " + e.getMessage() + "\"}")
                    .build();
        }
    }
}
