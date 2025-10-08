package com.danp1t.rest;

import com.danp1t.bean.Coordinates;
import com.danp1t.service.SimpleService;

import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/create/coordinates")
public class TestResource {

    @Inject
    private SimpleService simpleService;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response test(Coordinates response) {
        String message = response.toString();
        String jsonResponse = String.format(
                "{\"message\": \"%s\", \"status\": \"CDI is working!\"}",
                message.replace("\"", "\\\"")
        );

        return Response.ok(jsonResponse).build();
    }
}