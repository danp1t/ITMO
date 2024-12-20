package org.example.backend;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/register")
public class UserResource {

    @Inject
    private UserRegistrationService userRegistrationService;

    @POST
    @Consumes("application/json")
    public Response registerUser(UserDTO userDTO) {
        try {
            userRegistrationService.registerUser(userDTO.getLogin(), userDTO.getPassword());
            return Response.ok().entity("Пользователь зарегистрирован").build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Ошибка регистрации").build();
        }
    }
}

