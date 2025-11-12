package com.danp1t.controller;

import com.danp1t.dto.OrganizationDTO;
import com.danp1t.model.Organization;
import com.danp1t.service.OrganizationService;
import com.danp1t.websocket.OrganizationsWebSocket;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/organization")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrganizationController {

    @Inject
    private OrganizationService organizationService;

    @GET
    public Response getAllOrganizations() {
        try {
            List<Organization> organizations = organizationService.getAllOrganizations();

            JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
            for (Organization org : organizations) {
                arrayBuilder.add(Json.createObjectBuilder()
                        .add("id", org.getId())
                        .add("name", org.getName())
                        .add("coordinates", org.getCoordinates().getId())
                        .add("officialAddress", org.getOfficialAddress().getId())
                        .add("postalAddress", org.getPostalAddress().getId())
                        .add("annualTurnover", org.getAnnualTurnover())
                        .add("employeesCount", org.getEmployeesCount())
                        .add("rating", org.getRating())
                        .add("type", org.getType().toString())
                        .add("creationDate", org.getCreationDate().toString())
                        .build());
            }

            return Response.ok(arrayBuilder.build()).build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Ошибка при получении организаций: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @POST
    public Response createOrganization(Organization organization) {
        try {
            Organization savedOrganization = organizationService.createOrganization(organization);

            JsonObject jsonResponse = Json.createObjectBuilder()
                    .add("id", savedOrganization.getId())
                    .add("name", savedOrganization.getName())
                    .add("annualTurnover", savedOrganization.getAnnualTurnover())
                    .add("employeesCount", savedOrganization.getEmployeesCount())
                    .add("rating", savedOrganization.getRating())
                    .add("type", savedOrganization.getType().toString())
                    .build();

            OrganizationsWebSocket.notifyTableUpdate();
            return Response.ok(jsonResponse.toString()).build();

        } catch (Exception e) {
            JsonObject errorResponse = Json.createObjectBuilder()
                    .add("error", e.getMessage())
                    .build();

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorResponse.toString())
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response updateOrganization(@PathParam("id") Integer id, @Valid OrganizationDTO organizationUpdateDto) {
        try {
            Organization updatedOrganization = organizationService.updateOrganization(id, organizationUpdateDto);

            if (updatedOrganization == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\": \"Организация не найдена по ID: " + id + "\"}")
                        .build();
            }

            OrganizationDTO responseDto = convertToOrganizationSearchDTO(updatedOrganization);
            OrganizationsWebSocket.notifyTableUpdate();
            return Response.ok(responseDto).build();

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

    @DELETE
    @Path("/{id}")
    public Response deleteOrganization(@PathParam("id") Integer id) {
        try {
            boolean deleted = organizationService.deleteOrganization(id);

            if (!deleted) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("Организация не найдена по ID: " + id)
                        .build();
            }

            OrganizationsWebSocket.notifyTableUpdate();
            return Response.ok()
                    .entity("{\"message\": \"Организация успешно удалена\"}")
                    .build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\": \"Ошибка при удалении организации: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @POST
    @Path("/avg-rating")
    public Response calculateAverageRating() {
        try {
            Double averageRating = organizationService.calculateAverageRating();

            Map<String, Object> response = new HashMap<>();
            response.put("averageRating", averageRating);
            response.put("message", "Средний рейтинг успешно рассчитан");

            return Response.ok(response).build();

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка при расчете среднего рейтинга: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorResponse)
                    .build();
        }
    }

    @POST
    @Path("/search-by-name")
    public Response searchOrganizationsByName(Map<String, String> request) {
        try {
            String substring = request.get("substring");
            if (substring == null || substring.trim().isEmpty()) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Подстрока для поиска не может быть пустой");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(errorResponse)
                        .build();
            }

            List<OrganizationDTO> organizations = organizationService.findOrganizationsByNameStartingWith(substring);

            Map<String, Object> response = new HashMap<>();
            response.put("organizations", organizations);
            response.put("count", organizations.size());
            response.put("message", "Поиск выполнен успешно");

            return Response.ok(response).build();

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка при поиске по имени: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorResponse)
                    .build();
        }
    }

    @POST
    @Path("/filter-by-address")
    public Response filterOrganizationsByAddress(Map<String, Object> request) {
        try {
            Long minAddressId = null;
            if (request.containsKey("minAddressId")) {
                Object value = request.get("minAddressId");
                if (value instanceof Number) {
                    minAddressId = ((Number) value).longValue();
                } else if (value instanceof String) {
                    minAddressId = Long.parseLong((String) value);
                }
            }

            if (minAddressId == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "ID адреса для фильтрации не может быть пустым");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(errorResponse)
                        .build();
            }

            List<OrganizationDTO> organizations = organizationService.findOrganizationsByPostalAddressGreaterThan(minAddressId);

            Map<String, Object> response = new HashMap<>();
            response.put("organizations", organizations);
            response.put("count", organizations.size());
            response.put("message", "Фильтрация выполнена успешно");

            return Response.ok(response).build();

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка при фильтрации по адресу: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorResponse)
                    .build();
        }
    }

    @POST
    @Path("/merge")
    public Response mergeOrganizations(MergeRequest mergeRequest) {
        try {
            if (mergeRequest.getFirstOrgId() == null || mergeRequest.getSecondOrgId() == null ||
                    mergeRequest.getNewName() == null || mergeRequest.getNewName().trim().isEmpty() ||
                    mergeRequest.getNewAddressId() == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Все поля обязательны для заполнения");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(errorResponse)
                        .build();
            }

            Organization mergedOrganization = organizationService.mergeOrganizations(
                    mergeRequest.getFirstOrgId(),
                    mergeRequest.getSecondOrgId(),
                    mergeRequest.getNewName(),
                    mergeRequest.getNewAddressId()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("organization", mergedOrganization);
            response.put("message", "Организации успешно объединены в новую организацию с ID: " + mergedOrganization.getId());

            return Response.ok(response).build();

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка при объединении организаций: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorResponse)
                    .build();
        }
    }

    @POST
    @Path("/absorb")
    public Response absorbOrganization(AbsorbRequest absorbRequest) {
        try {
            if (absorbRequest.getAbsorbingOrgId() == null || absorbRequest.getAbsorbedOrgId() == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "ID обеих организаций обязательны");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(errorResponse)
                        .build();
            }

            Organization resultOrganization = organizationService.absorbOrganization(
                    absorbRequest.getAbsorbingOrgId(),
                    absorbRequest.getAbsorbedOrgId()
            );

            Map<String, Object> response = new HashMap<>();
            response.put("organization", resultOrganization);
            response.put("message", "Организация " + absorbRequest.getAbsorbedOrgId() +
                    " успешно поглощена организацией " + absorbRequest.getAbsorbingOrgId());

            return Response.ok(response).build();

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка при поглощении организации: " + e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorResponse)
                    .build();
        }
    }

    private OrganizationDTO convertToOrganizationSearchDTO(Organization organization) {
        OrganizationDTO dto = new OrganizationDTO();
        dto.setId(organization.getId());
        dto.setName(organization.getName());

        if (organization.getAnnualTurnover() != null) {
            dto.setAnnualTurnover(organization.getAnnualTurnover().floatValue());
        }

        if (organization.getEmployeesCount() != null) {
            dto.setEmployeesCount(organization.getEmployeesCount().longValue());
        }

        if (organization.getRating() != null) {
            dto.setRating(organization.getRating().intValue());
        }

        dto.setType(organization.getType());

        if (organization.getCoordinates() != null) {
            dto.setCoordinates(organization.getCoordinates().getId());
        }

        if (organization.getOfficialAddress() != null) {
            dto.setOfficialAddress(organization.getOfficialAddress().getId());
        }

        if (organization.getPostalAddress() != null) {
            dto.setPostalAddress(organization.getPostalAddress().getId());
        }

        return dto;
    }

    public static class MergeRequest {
        private Long firstOrgId;
        private Long secondOrgId;
        private String newName;
        private Long newAddressId;

        public Long getFirstOrgId() { return firstOrgId; }
        public void setFirstOrgId(Long firstOrgId) { this.firstOrgId = firstOrgId; }

        public Long getSecondOrgId() { return secondOrgId; }
        public void setSecondOrgId(Long secondOrgId) { this.secondOrgId = secondOrgId; }

        public String getNewName() { return newName; }
        public void setNewName(String newName) { this.newName = newName; }

        public Long getNewAddressId() { return newAddressId; }
        public void setNewAddressId(Long newAddressId) { this.newAddressId = newAddressId; }
    }

    public static class AbsorbRequest {
        private Long absorbingOrgId;
        private Long absorbedOrgId;

        public Long getAbsorbingOrgId() { return absorbingOrgId; }
        public void setAbsorbingOrgId(Long absorbingOrgId) { this.absorbingOrgId = absorbingOrgId; }

        public Long getAbsorbedOrgId() { return absorbedOrgId; }
        public void setAbsorbedOrgId(Long absorbedOrgId) { this.absorbedOrgId = absorbedOrgId; }
    }
}