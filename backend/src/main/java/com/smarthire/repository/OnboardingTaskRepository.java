package com.smarthire.repository;

import com.smarthire.model.OnboardingTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OnboardingTaskRepository extends JpaRepository<OnboardingTask, Long> {
    List<OnboardingTask> findByEmployeeId(Long employeeId);

    @Modifying
    @Query("DELETE FROM OnboardingTask t WHERE t.employee.id = :employeeId")
    void deleteByEmployeeId(Long employeeId);
}
