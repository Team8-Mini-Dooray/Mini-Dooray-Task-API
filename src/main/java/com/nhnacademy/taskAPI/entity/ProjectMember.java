package com.nhnacademy.taskAPI.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "project_members",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_project_user",
                columnNames = {"project_id", "user_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_member_id")
    private Long projectMemberId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    @NotNull
    private Project project;

    @Column(name = "user_id", length = 50, nullable = false)
    @NotNull
    private String userId;

    public ProjectMember(Project project, String userId) {
        this.project = project;
        this.userId = userId;
    }
}
