package com.nhnacademy.taskAPI.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "projects")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Project {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Long projectId;

    @Column(name = "name", length = 100, nullable = false)
    @NotNull
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    @NotNull
    private ProjectStatus status = ProjectStatus.ACTIVE;

    @Column(name = "admin_id", length = 50, nullable = false)
    @NotNull
    private String adminId;

    @CreationTimestamp
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", insertable = false)
    private LocalDateTime updatedAt;

    public Project(String name, ProjectStatus status, String adminId) {
        this.name = name;
        this.status = status == null ? ProjectStatus.ACTIVE : status;
        this.adminId = adminId;
    }

    public void updateName(String name) {
        this.name = name;
    }

    public void updateStatus(ProjectStatus status) {
        this.status = status;
    }

}
