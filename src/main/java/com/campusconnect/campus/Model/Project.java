package com.campusconnect.campus.Model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name="project-db")
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String projectName;

    private String description;

    @ManyToOne                        // many projects can have one owner but reverse is not possible in this
    @JoinColumn(name = "owner_id")
    private User owner;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Boolean isActive; // for soft delete

    @PrePersist
    public void prePersist() {

        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
