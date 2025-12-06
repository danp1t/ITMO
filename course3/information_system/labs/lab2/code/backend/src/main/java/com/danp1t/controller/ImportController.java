package com.danp1t.controller;

import com.danp1t.model.ImportOperation;
import com.danp1t.model.User;
import com.danp1t.service.AuthService;
import com.danp1t.service.ImportService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("/import")
@Produces(MediaType.APPLICATION_JSON)
public class ImportController {

    @Inject
    private ImportService importService;

    @Inject
    private AuthService authService;

    @POST
    @Path("/xml")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response importXml(
            @FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail,
            @HeaderParam("Authorization") String authHeader) {

        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\": \"Требуется авторизация\"}")
                        .build();
            }

            String token = authHeader.substring(7);
            User user = authService.getUserFromToken(token);
            if (user == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\": \"Недействительный токен\"}")
                        .build();
            }

            String fileName = fileDetail.getFileName();
            if (fileName == null || !fileName.toLowerCase().endsWith(".xml")) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Поддерживаются только файлы формата XML");
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(errorResponse)
                        .build();
            }

            ImportOperation operation = importService.importOrganizationsFromXml(
                    fileInputStream, user, fileName);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Импорт успешно завершен");
            response.put("operationId", operation.getId());
            response.put("recordsAdded", operation.getRecordsAdded());
            response.put("status", operation.getStatus());

            return Response.ok(response).build();

        } catch (RuntimeException e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorResponse)
                    .build();
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка импорта: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(errorResponse)
                    .build();
        }
    }

    @GET
    @Path("/history")
    public Response getImportHistory(@HeaderParam("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\": \"Требуется авторизация\"}")
                        .build();
            }

            String token = authHeader.substring(7);
            User user = authService.getUserFromToken(token);
            if (user == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity("{\"error\": \"Недействительный токен\"}")
                        .build();
            }

            List<ImportOperation> history = importService.getImportHistory(user);

            List<Map<String, Object>> historyDto = history.stream()
                    .map(op -> {
                        Map<String, Object> dto = new HashMap<>();
                        dto.put("id", op.getId());
                        dto.put("fileName", op.getFileName());
                        dto.put("importDate", op.getImportDate().toString());
                        dto.put("status", op.getStatus());
                        dto.put("recordsAdded", op.getRecordsAdded());
                        dto.put("errorMessage", op.getErrorMessage());
                        dto.put("user", op.getUser().getUsername());
                        return dto;
                    })
                    .toList();

            Map<String, Object> response = new HashMap<>();
            response.put("history", historyDto);
            response.put("count", historyDto.size());

            return Response.ok(response).build();

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Ошибка получения истории: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(errorResponse)
                    .build();
        }
    }
}