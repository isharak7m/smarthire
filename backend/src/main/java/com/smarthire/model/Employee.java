package com.smarthire.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Full name is required")
    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    @Column(nullable = false, unique = true)
    private String email;

    private String role;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING;

    @Column(name = "avatar_initials", length = 3)
    private String avatarInitials;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Status {
        PENDING, ACTIVE, COMPLETED
    }

    // Manual getters/setters for Lombok backup
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
    
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    
    public String getAvatarInitials() { return avatarInitials; }
    public void setAvatarInitials(String avatarInitials) { this.avatarInitials = avatarInitials; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
