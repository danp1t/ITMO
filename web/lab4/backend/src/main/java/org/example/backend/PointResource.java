package org.example.backend;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;

@Path("/point")
public class PointResource {


    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public Response receivePoint(PointDTO pointDTO, @Context HttpHeaders headers) {
        try {
            //processPoint(pointDTO.getX(), pointDTO.getY(), pointDTO.getR());
            return Response.ok()
                    .entity("{\"message\": \"Point received successfully\"}")
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
                    .header("Access-Control-Allow-Headers", "Content-Type")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Error processing point\"}")
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


    public void processPoint(double x, double y, double r) {
        System.out.println("Received point: (" + x + ", " + y + ", " + r + ")");
    }
}
