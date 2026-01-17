package com.danp1t.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.InputStream;
import java.io.IOException;

@ApplicationScoped
public class TransactionalFileService {

    @Inject
    private MinioService minioService;

    /**
     * Сохраняет файл с транзакционной семантикой
     * @return ключ файла
     */
    public String saveFileWithTransaction(InputStream inputStream, String fileName,
                                          String contentType, Long userId, Long operationId) throws IOException {

        // Генерируем временный ключ с префиксом "pending_"
        String tempKey = "pending_" + System.currentTimeMillis() + "_" + operationId + "_" + fileName;

        // Сохраняем файл с указанным ключом (не генерируем новое имя)
        minioService.uploadFileWithKey(inputStream, tempKey, contentType, userId, fileName);

        // Помечаем файл как временный (добавляем теги)
        minioService.addFileTags(tempKey, "status", "pending");
        minioService.addFileTags(tempKey, "operation_id", String.valueOf(operationId));

        return tempKey;
    }

    /**
     * Подтверждает сохранение файла (перемещает из временного в постоянное)
     */
    public void confirmFileSave(String tempKey) throws IOException {
        if (tempKey == null || !tempKey.startsWith("pending_")) {
            return;
        }

        // Убираем префикс "pending_" для получения постоянного ключа
        String permanentKey = tempKey.replaceFirst("pending_", "");

        // Копируем файл на постоянное место
        minioService.copyFile(tempKey, permanentKey);

        // Удаляем временный файл
        minioService.deleteFile(tempKey);

        // Обновляем теги
        minioService.addFileTags(permanentKey, "status", "confirmed");
    }

    /**
     * Откатывает сохранение файла (удаляет временный файл)
     */
    public void rollbackFileSave(String tempKey) throws IOException {
        if (tempKey == null || !tempKey.startsWith("pending_")) {
            return;
        }

        try {
            // Удаляем временный файл
            minioService.deleteFile(tempKey);
        } catch (Exception e) {
            // Логируем, но не прерываем
            System.err.println("Failed to rollback file: " + e.getMessage());
        }
    }

    /**
     * Получает постоянный ключ из временного
     */
    public String getPermanentKey(String tempKey) {
        if (tempKey == null || !tempKey.startsWith("pending_")) {
            return tempKey;
        }
        return tempKey.replaceFirst("pending_", "");
    }
}