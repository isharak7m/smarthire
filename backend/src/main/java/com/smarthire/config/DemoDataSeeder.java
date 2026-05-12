package com.smarthire.config;

import com.smarthire.model.Department;
import com.smarthire.model.Employee;
import com.smarthire.model.OnboardingTask;
import com.smarthire.repository.DepartmentRepository;
import com.smarthire.repository.EmployeeRepository;
import com.smarthire.repository.OnboardingTaskRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class DemoDataSeeder {

    @Bean
    public CommandLineRunner seedDemoData(
            DepartmentRepository departmentRepository,
            EmployeeRepository employeeRepository,
            OnboardingTaskRepository onboardingTaskRepository
    ) {
        return args -> {
            if (departmentRepository.count() == 0) {
                departmentRepository.saveAll(List.of(
                        department("Engineering", "Arjun Mehta"),
                        department("Human Resources", "Priya Nair"),
                        department("Finance", "Ramesh Iyer"),
                        department("Marketing", "Sneha Kapoor"),
                        department("Design", "Anika Patel")
                ));
            }

            if (employeeRepository.count() == 0) {
                seedEmployees(departmentRepository, employeeRepository, onboardingTaskRepository);
            }
        };
    }

    private void seedEmployees(
            DepartmentRepository departmentRepository,
            EmployeeRepository employeeRepository,
            OnboardingTaskRepository onboardingTaskRepository
    ) {
        Map<String, Department> departmentsByName = new LinkedHashMap<>();
        departmentRepository.findAll().forEach(dept -> departmentsByName.put(dept.getName(), dept));

        List<Employee> employees = List.of(
                employee("Kiran Raj", "kiran.raj@smarthire.io", "Software Engineer", departmentsByName.get("Engineering"), LocalDate.parse("2024-01-15"), Employee.Status.ACTIVE),
                employee("Divya Sharma", "divya.sharma@smarthire.io", "HR Coordinator", departmentsByName.get("Human Resources"), LocalDate.parse("2024-02-01"), Employee.Status.COMPLETED),
                employee("Arun Pillai", "arun.pillai@smarthire.io", "Financial Analyst", departmentsByName.get("Finance"), LocalDate.parse("2024-03-10"), Employee.Status.PENDING),
                employee("Meera Krishnan", "meera.k@smarthire.io", "Marketing Lead", departmentsByName.get("Marketing"), LocalDate.parse("2024-04-01"), Employee.Status.ACTIVE),
                employee("Rahul Verma", "rahul.v@smarthire.io", "UX Designer", departmentsByName.get("Design"), LocalDate.parse("2024-04-15"), Employee.Status.PENDING),
                employee("Sanya Gupta", "sanya.g@smarthire.io", "Backend Engineer", departmentsByName.get("Engineering"), LocalDate.parse("2024-05-01"), Employee.Status.ACTIVE),
                employee("Dev Nair", "dev.n@smarthire.io", "Finance Analyst", departmentsByName.get("Finance"), LocalDate.parse("2024-05-10"), Employee.Status.COMPLETED),
                employee("Tanvi Roy", "tanvi.r@smarthire.io", "HR Manager", departmentsByName.get("Human Resources"), LocalDate.parse("2024-06-01"), Employee.Status.PENDING),
                employee("Aryan Mehta", "aryan.m@smarthire.io", "Data Engineer", departmentsByName.get("Engineering"), LocalDate.parse("2024-06-15"), Employee.Status.ACTIVE),
                employee("Pooja Singh", "pooja.s@smarthire.io", "Content Strategist", departmentsByName.get("Marketing"), LocalDate.parse("2024-07-01"), Employee.Status.ACTIVE),
                employee("Nikhil Das", "nikhil.d@smarthire.io", "DevOps Engineer", departmentsByName.get("Engineering"), LocalDate.parse("2024-07-15"), Employee.Status.PENDING),
                employee("Lakshmi Rao", "lakshmi.r@smarthire.io", "Product Manager", departmentsByName.get("Design"), LocalDate.parse("2024-08-01"), Employee.Status.ACTIVE)
        );

        for (Employee employee : employees) {
            Employee saved = employeeRepository.save(employee);
            seedTasks(saved, onboardingTaskRepository);
        }
    }

    private void seedTasks(Employee employee, OnboardingTaskRepository onboardingTaskRepository) {
        List<String[]> templates = List.of(
                new String[]{"Set up laptop and dev environment", "IT"},
                new String[]{"Complete HR documentation", "HR"},
                new String[]{"Security & compliance training", "Training"},
                new String[]{"Meet your team lead", "Orientation"},
                new String[]{"Access provisioning", "IT"}
        );

        int doneCount = switch (employee.getStatus()) {
            case COMPLETED -> templates.size();
            case ACTIVE -> 4;
            case PENDING -> 0;
        };

        for (int index = 0; index < templates.size(); index += 1) {
            String[] template = templates.get(index);
            OnboardingTask task = new OnboardingTask();
            task.setEmployee(employee);
            task.setTaskName(template[0]);
            task.setCategory(template[1]);
            task.setDone(index < doneCount);
            onboardingTaskRepository.save(task);
        }
    }

    private Department department(String name, String head) {
        Department department = new Department();
        department.setName(name);
        department.setHead(head);
        return department;
    }

    private Employee employee(
            String fullName,
            String email,
            String role,
            Department department,
            LocalDate startDate,
            Employee.Status status
    ) {
        Employee employee = new Employee();
        employee.setFullName(fullName);
        employee.setEmail(email);
        employee.setRole(role);
        employee.setDepartment(department);
        employee.setStartDate(startDate);
        employee.setStatus(status);
        employee.setAvatarInitials(initials(fullName));
        return employee;
    }

    private String initials(String fullName) {
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return (parts[0].charAt(0) + "" + parts[parts.length - 1].charAt(0)).toUpperCase();
    }
}
