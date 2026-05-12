package com.smarthire.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "onboarding_tasks")
public class OnboardingTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(name = "task_name")
    private String taskName;

    private String category;

    @Column(name = "is_done")
    private boolean done = false;

    // Manual getters/setters for Lombok backup
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    
    public String getTaskName() { return taskName; }
    public void setTaskName(String taskName) { this.taskName = taskName; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public boolean isDone() { return done; }
    public void setDone(boolean done) { this.done = done; }
}
