package com.danp1t.rest;

import com.danp1t.bean.Organization;
import com.danp1t.bean.OrganizationSearchDTO;
import com.danp1t.service.SpecialOperationsService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/special")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SpecialOperationsResource {

    @Inject
    private SpecialOperationsService specialOperationsService;

    @POST
    @Path("/avg-rating")
    public Response calculateAverageRating() {
        try {
            Double averageRating = specialOperationsService.calculateAverageRating();

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

            List<OrganizationSearchDTO> organizations = specialOperationsService.findOrganizationsByNameStartingWith(substring);

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

            List<OrganizationSearchDTO> organizations = specialOperationsService.findOrganizationsByPostalAddressGreaterThan(minAddressId);

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
    @Path("/merge-organizations")
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

            Organization mergedOrganization = specialOperationsService.mergeOrganizations(
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
    @Path("/absorb-organization")
    public Response absorbOrganization(AbsorbRequest absorbRequest) {
        try {
            if (absorbRequest.getAbsorbingOrgId() == null || absorbRequest.getAbsorbedOrgId() == null) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "ID обеих организаций обязательны");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(errorResponse)
                        .build();
            }

            Organization resultOrganization = specialOperationsService.absorbOrganization(
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

    public static class MergeRequest {
        private Long firstOrgId;
        private Long secondOrgId;
        private String newName;
        private Long newAddressId;

        // Геттеры и сеттеры
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