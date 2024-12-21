package org.example.backend;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;

@Path("/register")
public class UserResource {

    @Inject
    private UserRegistrationService userRegistrationService;


    @POST
    @Consumes("application/json")
    @Produces("application/json") // Убедитесь, что вы указываете, что возвращаете JSON
    public Response registerUser(UserDTO userDTO, @Context HttpHeaders headers) {
        try {
            userRegistrationService.registerUser(userDTO.getLogin(), userDTO.getPassword());
            return Response.ok()
                    .entity("{\"message\": \"Пользователь зарегистрирован\"}") // Возвращаем JSON
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
                    .header("Access-Control-Allow-Headers", "Content-Type")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Ошибка регистрации\"}") // Возвращаем JSON
                    .header("Access-Control-Allow-Origin", "*")
                    .build();
        }
    }


    @OPTIONS
    public Response options() {
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
                .header("Access-Control-Allow-Headers", "Content-Type")
                .build();
    }
}