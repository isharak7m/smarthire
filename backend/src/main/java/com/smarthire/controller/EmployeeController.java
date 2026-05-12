package com.smarthire.controller;

import com.smarthire.model.Department;
import com.smarthire.model.Employee;
import com.smarthire.model.OnboardingTask;
import com.smarthire.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller — demonstrates:
 * - CRUD endpoints
 * - Pagination (page/size/sort params)
 * - Search and filter
 * - Bean Validation (@Valid)
 * - Proper HTTP status codes
 */
@RestController
@RequestMapping("/api")
public class EmployeeController {

    @Autowired
    private EmployeeService service;

    // ── Employees (paginated) ─────────────────────────────────────
    @GetMapping("/employees")
    public ResponseEntity<?> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sort) {

        if (search != null && !search.isBlank()) {
            return ResponseEntity.ok(service.search(search));
        }
        if (status != null && !status.isBlank()) {
            Page<Employee> result = service.filterByStatus(status,
                    PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort)));
            return ResponseEntity.ok(result);
        }
        return ResponseEntity.ok(service.getAllPaged(page, size, sort));
    }

    @GetMapping("/employees/all")
    public List<Employee> getAllList() {
        return service.getAll();
    }

    @GetMapping("/employees/{id}")
    public Employee getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping("/employees")
    public ResponseEntity<Employee> create(@Valid @RequestBody Employee emp) {
        Employee created = service.create(emp);
        return ResponseEntity.status(201).body(created);
    }

    @PutMapping("/employees/{id}")
    public Employee update(@PathVariable Long id, @Valid @RequestBody Employee emp) {
        return service.update(id, emp);
    }

    @DeleteMapping("/employees/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ── Tasks ─────────────────────────────────────────────────────
    @GetMapping("/employees/{id}/tasks")
    public List<OnboardingTask> getTasks(@PathVariable Long id) {
        return service.getTasks(id);
    }

    @PatchMapping("/tasks/{taskId}")
    public OnboardingTask updateTask(@PathVariable Long taskId,
                                     @RequestBody Map<String, Boolean> body) {
        return service.updateTask(taskId, body.get("done"));
    }

    // ── Stats (returns data for Chart.js analytics) ───────────────
    @GetMapping("/stats")
    public Map<String, Object> getStats() {
        return service.getStats();
    }

    // ── Departments ───────────────────────────────────────────────
    @GetMapping("/departments")
    public List<Department> getDepartments() {
        return service.getDepartments();
    }
}
