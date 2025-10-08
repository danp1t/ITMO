package com.danp1t.rest;

import com.danp1t.bean.Address;
import com.danp1t.bean.Coordinates;
import com.danp1t.bean.Location;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Path("/get")
@Produces(MediaType.APPLICATION_JSON)
public class GetEntities {

    @GET
    @Path("/address")
    public Response getAddresses() {
        try {
            // TODO: Замените на реальные данные из БД
            List<Address> addresses = new ArrayList<>();

            // Пример данных для тестирования
            Address addr1 = new Address();
            addr1.setId(1L);
            addr1.setStreet("ул. Примерная");
            addr1.setZipCode("123456");

            Address addr2 = new Address();
            addr2.setId(2L);
            addr2.setStreet("ул. Тестовая");
            addr2.setZipCode("654321");

            addresses.add(addr1);
            addresses.add(addr2);

            // Формируем JSON ответ
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            for (Address address : addresses) {
                arrayBuilder.add(Json.createObjectBuilder()
                        .add("id", address.getId())
                        .add("street", address.getStreet())
                        .add("zipCode", address.getZipCode())
                        .add("displayName", address.getStreet() + ", " + address.getZipCode())
                        .build());
            }

            JsonArray jsonArray = arrayBuilder.build();

            return Response.ok(jsonArray).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Ошибка при получении адресов: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/coordinates")
    public Response getCoordinates() {
        try {
            // TODO: Замените на реальные данные из БД
            List<Coordinates> coordinatesList = new ArrayList<>();

            // Пример данных для тестирования
            Coordinates coord1 = new Coordinates();
            coord1.setId(1L);
            coord1.setX(10.5f);
            coord1.setY(20.3);

            Coordinates coord2 = new Coordinates();
            coord2.setId(2L);
            coord2.setX(15.2f);
            coord2.setY(25.1);

            coordinatesList.add(coord1);
            coordinatesList.add(coord2);

            // Формируем JSON ответ
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            for (Coordinates coord : coordinatesList) {
                arrayBuilder.add(Json.createObjectBuilder()
                        .add("id", coord.getId())
                        .add("x", coord.getX())
                        .add("y", coord.getY())
                        .add("displayName", "X: " + coord.getX() + ", Y: " + coord.getY())
                        .build());
            }

            JsonArray jsonArray = arrayBuilder.build();

            return Response.ok(jsonArray).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Ошибка при получении координат: " + e.getMessage())
                    .build();
        }
    }

    @GET
    @Path("/location")
    public Response getLocations() {
        try {
            // TODO: Замените на реальные данные из БД
            List<Location> locations = new ArrayList<>();

            // Пример данных для тестирования
            Location loc1 = new Location();
            loc1.setId(1L);
            loc1.setX(100.0);
            loc1.setY(200.0f);
            loc1.setZ(50.0);
            loc1.setName("Москва");

            Location loc2 = new Location();
            loc2.setId(2L);
            loc2.setX(300.0);
            loc2.setY(400.0f);
            loc2.setZ(60.0);
            loc2.setName("Санкт-Петербург");

            locations.add(loc1);
            locations.add(loc2);

            // Формируем JSON ответ
            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            for (Location location : locations) {
                arrayBuilder.add(Json.createObjectBuilder()
                        .add("id", location.getId())
                        .add("x", location.getX())
                        .add("y", location.getY())
                        .add("z", location.getZ())
                        .add("name", location.getName())
                        .add("displayName", location.getName())
                        .build());
            }

            JsonArray jsonArray = arrayBuilder.build();

            return Response.ok(jsonArray).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Ошибка при получении локаций: " + e.getMessage())
                    .build();
        }
    }
}