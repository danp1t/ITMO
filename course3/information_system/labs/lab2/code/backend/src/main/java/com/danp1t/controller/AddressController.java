package com.danp1t.controller;

import com.danp1t.model.Address;
import com.danp1t.service.AddressService;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/address")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AddressController {

    @Inject
    private AddressService addressService;

    @GET
    public Response getAllAddresses() {
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

            return Response.ok(arrayBuilder.build()).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Ошибка при получении адресов: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    public Response getAddressById(@PathParam("id") Long id) {
        try {
            Address address = addressService.getAddressById(id);

            if (address == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Адрес не найден по id: " + id)
                        .build();
            }

            return Response.ok(address).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Ошибка при получении адреса: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @POST
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

    @PUT
    @Path("/{id}")
    public Response updateAddress(@PathParam("id") Long id, Address address) {
        address.setId(id);
        try {
            if (!id.equals(address.getId())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("ID в Path не совпадает с ID Body")
                        .build();
            }

            Address updatedAddress = addressService.updateAddress(address);

            if (updatedAddress == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Адрес не найден по ID: " + id)
                        .build();
            }

            return Response.ok(updatedAddress).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Ошибка при обновлении адреса: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteAddress(@PathParam("id") Long id) {
        try {
            boolean deleted = addressService.deleteAddress(id);

            if (!deleted) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Адрес не найден по ID: " + id)
                        .build();
            }

            return Response.ok()
                    .entity("{\"message\": \"Адрес успешно удален\"}")
                    .build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Ошибка при удалении адреса: " + e.getMessage() + "\"}")
                    .build();
        }
    }
}