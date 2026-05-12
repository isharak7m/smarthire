package com.smarthire.service;

import com.smarthire.model.Department;
import com.smarthire.model.Employee;
import com.smarthire.model.OnboardingTask;
import com.smarthire.repository.DepartmentRepository;
import com.smarthire.repository.EmployeeRepository;
import com.smarthire.repository.OnboardingTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Core business logic layer.
 * Demonstrates: @Transactional, pagination, service layer pattern,
 * custom JPQL queries, and integration with EmailService.
 */
@Service
public class EmployeeService {

    @Autowired private EmployeeRepository employeeRepo;
    @Autowired private DepartmentRepository deptRepo;
    @Autowired private OnboardingTaskRepository taskRepo;
    @Autowired private EmailService emailService;

    // ── Read ─────────────────────────────────────────────────────────

    /**
     * Paginated employee list — demonstrates Spring Data Pagination.
     * @param page  0-indexed page number
     * @param size  page size (defaults to 10)
     * @param sort  field to sort by (default: id)
     */
    public Page<Employee> getAllPaged(int page, int size, String sort) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by(Sort.Direction.DESC, sort.isEmpty() ? "id" : sort));
        return employeeRepo.findAll(pageable);
    }

    public List<Employee> getAll() {
        return employeeRepo.findAll();
    }

    public Employee getById(Long id) {
        return employeeRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found: " + id));
    }

    public List<Employee> search(String q) {
        return employeeRepo.search(q);
    }

    public Page<Employee> filterByStatus(String status, Pageable pageable) {
        return employeeRepo.findByStatus(Employee.Status.valueOf(status.toUpperCase()), pageable);
    }

    // ── Write ────────────────────────────────────────────────────────

    @Transactional
    public Employee create(Employee emp) {
        emp.setAvatarInitials(generateInitials(emp.getFullName()));
        Employee saved = employeeRepo.save(emp);
        createDefaultTasks(saved);
        // Fire-and-forget welcome email (async, non-blocking)
        emailService.sendWelcomeEmail(saved);
        return saved;
    }

    @Transactional
    public Employee update(Long id, Employee updated) {
        Employee existing = getById(id);
        existing.setFullName(updated.getFullName());
        existing.setEmail(updated.getEmail());
        existing.setRole(updated.getRole());
        existing.setDepartment(updated.getDepartment());
        existing.setStartDate(updated.getStartDate());
        existing.setStatus(updated.getStatus());
        existing.setAvatarInitials(generateInitials(updated.getFullName()));
        return employeeRepo.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        taskRepo.deleteByEmployeeId(id);
        employeeRepo.deleteById(id);
    }

    // ── Tasks ────────────────────────────────────────────────────────

    public List<OnboardingTask> getTasks(Long employeeId) {
        return taskRepo.findByEmployeeId(employeeId);
    }

    @Transactional
    public OnboardingTask updateTask(Long taskId, boolean done) {
        OnboardingTask task = taskRepo.findById(taskId).orElseThrow();
        task.setDone(done);
        OnboardingTask saved = taskRepo.save(task);
        updateEmployeeStatus(task.getEmployee().getId());
        return saved;
    }

    // ── Stats & Analytics ────────────────────────────────────────────

    public Map<String, Object> getStats() {
        long total     = employeeRepo.count();
        long pending   = employeeRepo.findByStatus(Employee.Status.PENDING).size();
        long active    = employeeRepo.findByStatus(Employee.Status.ACTIVE).size();
        long completed = employeeRepo.findByStatus(Employee.Status.COMPLETED).size();

        // Department breakdown for doughnut chart
        List<Object[]> deptRaw = employeeRepo.countByDepartment();
        Map<String, Long> byDepartment = new LinkedHashMap<>();
        for (Object[] row : deptRaw) {
            byDepartment.put((String) row[0], (Long) row[1]);
        }

        // Monthly hires for bar chart
        String[] months = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        Map<String, Long> byMonth = new LinkedHashMap<>();
        for (String m : months) byMonth.put(m, 0L);
        List<Object[]> monthRaw = employeeRepo.countByMonth();
        for (Object[] row : monthRaw) {
            int monthNum = ((Number) row[0]).intValue();
            byMonth.put(months[monthNum - 1], (Long) row[1]);
        }

        return Map.of(
            "total", total,
            "pending", pending,
            "active", active,
            "completed", completed,
            "byDepartment", byDepartment,
            "byMonth", byMonth
        );
    }

    public List<Department> getDepartments() {
        return deptRepo.findAll();
    }

    // ── Private helpers ──────────────────────────────────────────────

    private void createDefaultTasks(Employee emp) {
        List<String[]> defaults = Arrays.asList(
            new String[]{"Set up laptop and dev environment", "IT"},
            new String[]{"Complete HR documentation", "HR"},
            new String[]{"Security & compliance training", "Training"},
            new String[]{"Meet your team lead", "Orientation"},
            new String[]{"Access provisioning (email, tools)", "IT"}
        );
        for (String[] t : defaults) {
            OnboardingTask task = new OnboardingTask();
            task.setEmployee(emp);
            task.setTaskName(t[0]);
            task.setCategory(t[1]);
            taskRepo.save(task);
        }
    }

    private void updateEmployeeStatus(Long empId) {
        List<OnboardingTask> tasks = taskRepo.findByEmployeeId(empId);
        long done = tasks.stream().filter(OnboardingTask::isDone).count();
        Employee emp = getById(empId);
        if (done == 0) emp.setStatus(Employee.Status.PENDING);
        else if (done == tasks.size()) {
            emp.setStatus(Employee.Status.COMPLETED);
            emailService.sendOnboardingCompleteEmail(emp);
        } else {
            emp.setStatus(Employee.Status.ACTIVE);
        }
        employeeRepo.save(emp);
    }

    private String generateInitials(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1)
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        return (parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0)).toUpperCase();
    }
}
