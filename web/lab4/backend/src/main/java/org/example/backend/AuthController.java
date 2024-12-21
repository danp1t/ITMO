package org.example.backend;

import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/auth")
public class AuthController {

    @EJB
    private AuthService authService;

    @EJB
    private TokenUtil tokenService;

    @OPTIONS
    @Path("/login")
    public Response options() {
        return Response.ok()
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
                .header("Access-Control-Allow-Headers", "Content-Type")
                .build();
    }

    @POST
    @Path("/login") // Убедитесь, что путь указан
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(UserDTO userDTO) {
        System.out.println(userDTO);
        System.out.println(userDTO.getLogin());
        System.out.println(userDTO.getPassword());
        Users user = authService.authenticate(userDTO.getLogin(), userDTO.getPassword());

        if (user != null) {
            String token = tokenService.generateToken(user);
            return Response.ok(new AuthResponse(token))
                    .header("Access-Control-Allow-Origin", "*")
                    .build();
        }

        return Response.status(Response.Status.UNAUTHORIZED)
                .entity("Неверный логин или пароль")
                .header("Access-Control-Allow-Origin", "*")
                .build();
    }
}

