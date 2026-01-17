package com.danp1t.model;

import com.danp1t.interceptor.CacheStatistic;

import java.time.LocalDateTime;

@CacheStatistic
public class ImportOperation {

    private Long id;
    private String fileName;
    private String fileKey;
    private LocalDateTime importDate;
    private String status;
    private Integer recordsAdded;
    private String errorMessage;
    private User user;

    public ImportOperation() {
        this.importDate = LocalDateTime.now();
    }

    public ImportOperation(String fileName, User user) {
        this();
        this.fileName = fileName;
        this.user = user;
        this.status = "PROCESSING";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public LocalDateTime getImportDate() { return importDate; }
    public void setImportDate(LocalDateTime importDate) { this.importDate = importDate; }

    public String getFileKey() { return fileKey; }
    public void setFileKey(String fileKey) { this.fileKey = fileKey; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getRecordsAdded() { return recordsAdded; }
    public void setRecordsAdded(Integer recordsAdded) { this.recordsAdded = recordsAdded; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}