-- SmartHire v2 Schema
-- Run once to set up MySQL

CREATE DATABASE IF NOT EXISTS smarthire;
USE smarthire;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) DEFAULT 'ROLE_USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS departments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    head VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS employees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(150) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    role VARCHAR(100),
    department_id BIGINT,
    start_date DATE,
    status ENUM('PENDING', 'ACTIVE', 'COMPLETED') DEFAULT 'PENDING',
    avatar_initials VARCHAR(3),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (department_id) REFERENCES departments(id)
);

CREATE TABLE IF NOT EXISTS onboarding_tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    task_name VARCHAR(200) NOT NULL,
    category VARCHAR(50),
    is_done BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
);

-- Seed departments
INSERT IGNORE INTO departments (id, name, head) VALUES
(1, 'Engineering', 'Arjun Mehta'),
(2, 'Human Resources', 'Priya Nair'),
(3, 'Finance', 'Ramesh Iyer'),
(4, 'Marketing', 'Sneha Kapoor'),
(5, 'Design', 'Anika Patel');

-- Default admin user (password: Admin@123) — hash generated with BCrypt strength 12
INSERT IGNORE INTO users (id, username, email, password, role) VALUES
(1, 'admin', 'admin@smarthire.io',
 '$2a$12$n9GtCQn.wBJEL9mVV/jOZODvXkmb3XJAqtH2xL7C2RQTQE7HmJEBm',
 'ROLE_ADMIN');

INSERT IGNORE INTO employees (id, full_name, email, role, department_id, start_date, status, avatar_initials) VALUES
(1, 'Kiran Raj', 'kiran.raj@smarthire.io', 'Software Engineer', 1, '2024-01-15', 'ACTIVE', 'KR'),
(2, 'Divya Sharma', 'divya.sharma@smarthire.io', 'HR Coordinator', 2, '2024-02-01', 'COMPLETED', 'DS'),
(3, 'Arun Pillai', 'arun.pillai@smarthire.io', 'Financial Analyst', 3, '2024-03-10', 'PENDING', 'AP'),
(4, 'Meera Krishnan', 'meera.k@smarthire.io', 'Marketing Lead', 4, '2024-04-01', 'ACTIVE', 'MK'),
(5, 'Rahul Verma', 'rahul.v@smarthire.io', 'UX Designer', 5, '2024-04-15', 'PENDING', 'RV'),
(6, 'Sanya Gupta', 'sanya.g@smarthire.io', 'Backend Engineer', 1, '2024-05-01', 'ACTIVE', 'SG'),
(7, 'Dev Nair', 'dev.n@smarthire.io', 'Finance Analyst', 3, '2024-05-10', 'COMPLETED', 'DN'),
(8, 'Tanvi Roy', 'tanvi.r@smarthire.io', 'HR Manager', 2, '2024-06-01', 'PENDING', 'TR'),
(9, 'Aryan Mehta', 'aryan.m@smarthire.io', 'Data Engineer', 1, '2024-06-15', 'ACTIVE', 'AM'),
(10, 'Pooja Singh', 'pooja.s@smarthire.io', 'Content Strategist', 4, '2024-07-01', 'ACTIVE', 'PS'),
(11, 'Nikhil Das', 'nikhil.d@smarthire.io', 'DevOps Engineer', 1, '2024-07-15', 'PENDING', 'ND'),
(12, 'Lakshmi Rao', 'lakshmi.r@smarthire.io', 'Product Manager', 5, '2024-08-01', 'ACTIVE', 'LR');

INSERT IGNORE INTO onboarding_tasks (employee_id, task_name, category, is_done) VALUES
(1, 'Set up laptop and dev environment', 'IT', TRUE),
(1, 'Complete HR documentation', 'HR', TRUE),
(1, 'Security & compliance training', 'Training', FALSE),
(1, 'Meet your team lead', 'Orientation', TRUE),
(1, 'Access provisioning', 'IT', TRUE),
(2, 'Set up laptop and dev environment', 'IT', TRUE),
(2, 'Complete HR documentation', 'HR', TRUE),
(2, 'Security & compliance training', 'Training', TRUE),
(2, 'Meet your team lead', 'Orientation', TRUE),
(2, 'Access provisioning', 'IT', TRUE),
(3, 'Set up laptop and dev environment', 'IT', FALSE),
(3, 'Complete HR documentation', 'HR', FALSE),
(3, 'Security & compliance training', 'Training', FALSE),
(3, 'Meet your team lead', 'Orientation', FALSE),
(3, 'Access provisioning', 'IT', FALSE);
