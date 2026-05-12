-- SmartHire v2 H2 Data Script
-- Insert sample data for H2 development

-- Seed departments
INSERT INTO departments (id, name, head) VALUES
(1, 'Engineering', 'Arjun Mehta'),
(2, 'Human Resources', 'Priya Nair'),
(3, 'Finance', 'Ramesh Iyer'),
(4, 'Marketing', 'Sneha Kapoor'),
(5, 'Design', 'Anika Patel');

-- Default admin user (password: Admin@123) — hash generated with BCrypt strength 12
INSERT INTO users (id, username, email, password, role, created_at) VALUES
(1, 'admin', 'admin@smarthire.io',
 '$2a$12$n9GtCQn.wBJEL9mVV/jOZODvXkmb3XJAqtH2xL7C2RQTQE7HmJEBm',
 'ROLE_ADMIN', CURRENT_TIMESTAMP);

INSERT INTO employees (id, full_name, email, role, department_id, start_date, status, avatar_initials, created_at) VALUES
(1, 'Kiran Raj', 'kiran.raj@smarthire.io', 'Software Engineer', 1, '2024-01-15', 'ACTIVE', 'KR', CURRENT_TIMESTAMP),
(2, 'Divya Sharma', 'divya.sharma@smarthire.io', 'HR Coordinator', 2, '2024-02-01', 'COMPLETED', 'DS', CURRENT_TIMESTAMP),
(3, 'Arun Pillai', 'arun.pillai@smarthire.io', 'Financial Analyst', 3, '2024-03-10', 'PENDING', 'AP', CURRENT_TIMESTAMP),
(4, 'Meera Krishnan', 'meera.k@smarthire.io', 'Marketing Lead', 4, '2024-04-01', 'ACTIVE', 'MK', CURRENT_TIMESTAMP),
(5, 'Rahul Verma', 'rahul.v@smarthire.io', 'UX Designer', 5, '2024-04-15', 'PENDING', 'RV', CURRENT_TIMESTAMP),
(6, 'Sanya Gupta', 'sanya.g@smarthire.io', 'Backend Engineer', 1, '2024-05-01', 'ACTIVE', 'SG', CURRENT_TIMESTAMP),
(7, 'Dev Nair', 'dev.n@smarthire.io', 'Finance Analyst', 3, '2024-05-10', 'COMPLETED', 'DN', CURRENT_TIMESTAMP),
(8, 'Tanvi Roy', 'tanvi.r@smarthire.io', 'HR Manager', 2, '2024-06-01', 'PENDING', 'TR', CURRENT_TIMESTAMP),
(9, 'Aryan Mehta', 'aryan.m@smarthire.io', 'Data Engineer', 1, '2024-06-15', 'ACTIVE', 'AM', CURRENT_TIMESTAMP),
(10, 'Pooja Singh', 'pooja.s@smarthire.io', 'Content Strategist', 4, '2024-07-01', 'ACTIVE', 'PS', CURRENT_TIMESTAMP),
(11, 'Nikhil Das', 'nikhil.d@smarthire.io', 'DevOps Engineer', 1, '2024-07-15', 'PENDING', 'ND', CURRENT_TIMESTAMP),
(12, 'Lakshmi Rao', 'lakshmi.r@smarthire.io', 'Product Manager', 5, '2024-08-01', 'ACTIVE', 'LR', CURRENT_TIMESTAMP);

INSERT INTO onboarding_tasks (employee_id, task_name, category, is_done) VALUES
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
