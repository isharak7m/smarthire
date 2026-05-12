package com.smarthire.repository;

import com.smarthire.model.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // Full-text search across name, email, role — used for search bar
    @Query("SELECT e FROM Employee e WHERE " +
            "LOWER(e.fullName) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
            "LOWER(e.email)    LIKE LOWER(CONCAT('%',:q,'%')) OR " +
            "LOWER(e.role)     LIKE LOWER(CONCAT('%',:q,'%'))")
    List<Employee> search(@Param("q") String query);

    // Filter by status with pagination
    Page<Employee> findByStatus(Employee.Status status, Pageable pageable);

    // Simple list (no pagination) for stats
    List<Employee> findByStatus(Employee.Status status);

    // Department breakdown for analytics chart
    @Query("SELECT e.department.name, COUNT(e) FROM Employee e " +
            "WHERE e.department IS NOT NULL GROUP BY e.department.name")
    List<Object[]> countByDepartment();

    // Monthly hires for trend chart
    @Query("SELECT MONTH(e.startDate), COUNT(e) FROM Employee e " +
            "WHERE e.startDate IS NOT NULL GROUP BY MONTH(e.startDate) ORDER BY MONTH(e.startDate)")
    List<Object[]> countByMonth();
}
