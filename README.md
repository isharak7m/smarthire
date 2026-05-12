# SmartHire v2

SmartHire is a full-stack employee onboarding portal built to demonstrate practical backend and frontend engineering skills in an interview setting. It includes JWT authentication, employee management, onboarding task tracking, status analytics, and a responsive dashboard UI.

## Tech Stack

| Layer | Technology |
| --- | --- |
| Backend | Java 17, Spring Boot 3.2, Spring Security, Spring Data JPA |
| Authentication | JWT, BCrypt |
| Database | H2 for local demo, MySQL-ready configuration |
| Frontend | HTML5, CSS3, Vanilla JavaScript |
| Testing | JUnit 5, Spring Boot Test, MockMvc |
| Deployment | Render backend, Vercel frontend |

## Features

- JWT-based login and registration
- Employee CRUD with validation, pagination, filtering, and search
- Onboarding checklist tracking per employee
- Dashboard summary cards for onboarding status
- Responsive employee cards, employee table, and details modal
- Seeded demo data for a clean first run

## Run Locally

### Backend

```bash
cd backend
mvn spring-boot:run
```

API base URL: `http://localhost:8081/api`

### Frontend

Serve the `frontend` folder with Live Server or any static file server, then open the app in a browser.

### One-command local start

From the project root:

```powershell
powershell -ExecutionPolicy Bypass -File .\run-local.ps1
```

Frontend URL: `http://localhost:5500`

## Easiest Deploy

The simplest deploy for this repo is a single Render web service that serves both the frontend and backend from one URL.

1. Push this project to GitHub.
2. Sign in to Render.
3. Create a new Blueprint and select this repository.
4. Render will detect [render.yaml](./render.yaml) and create one `smarthire` web service.
5. Click deploy.

After the deploy finishes, open your Render URL and sign in with:

- Username: `admin`
- Password: `Admin@123`

Notes:

- The app now serves the frontend from Spring Boot, so you do not need a separate frontend host.
- On Render free web services, the app spins down after inactivity and cold starts on the next request.
- The current H2 setup is demo-friendly and resets whenever the free service restarts or redeploys.

## Demo Login

- Username: `admin`
- Password: `Admin@123`

## Tests

```bash
cd backend
mvn test
```

The test suite covers authentication flow and protected employee endpoints.

## Core API Endpoints

| Method | Endpoint | Purpose |
| --- | --- | --- |
| POST | `/api/auth/login` | Login and receive JWT |
| POST | `/api/auth/register` | Register and receive JWT |
| GET | `/api/auth/me` | Get current user |
| GET | `/api/employees` | List employees |
| POST | `/api/employees` | Create employee |
| GET | `/api/employees/{id}` | Employee details |
| DELETE | `/api/employees/{id}` | Delete employee |
| GET | `/api/employees/{id}/tasks` | List onboarding tasks |
| PATCH | `/api/tasks/{id}` | Toggle task completion |
| GET | `/api/stats` | Dashboard statistics |
| GET | `/api/departments` | Department list |

## Notes

- Local startup now loads sample departments, users, employees, and onboarding tasks automatically.
- Email is disabled by default and can be enabled with environment variables.
- CORS origins can be configured through `FRONTEND_URL`.
