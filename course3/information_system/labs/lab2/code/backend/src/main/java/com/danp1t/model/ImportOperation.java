package com.danp1t.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "import_operations")
public class ImportOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private LocalDateTime importDate;

    @Column(nullable = false)
    private String status; // SUCCESS, FAILED, PROCESSING

    @Column
    private Integer recordsAdded;

    @Column
    private String errorMessage;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
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

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public LocalDateTime getImportDate() { return importDate; }
    public void setImportDate(LocalDateTime importDate) { this.importDate = importDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getRecordsAdded() { return recordsAdded; }
    public void setRecordsAdded(Integer recordsAdded) { this.recordsAdded = recordsAdded; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}