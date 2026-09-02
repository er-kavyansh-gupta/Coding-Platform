# CodeBench — Online Coding Practice & Skill Assessment Platform

Full-stack implementation using: **Spring Boot + React + MySQL**, with a
sandboxed code execution engine (Java, Python, C++, JavaScript), problem bank with hints/editorials,
JWT auth, leaderboard and a personal progress dashboard.

```
coding-platform/
├── backend/     Spring Boot API (Java 17, Maven)
├── frontend/    React app (Vite)
└── docker-compose.yml
```

---

## 1. What's included

| Area | Status |
|---|---|
| JWT auth, role-based access (USER/ADMIN) | ✅ complete |
| Problem CRUD, tags, hints, unlock-after-N-failures editorial | ✅ complete |
| Code execution (compile + run per test case, timeout handling, verdicts) | ✅ complete — **two modes**, see §5 |
| Submission history, rate limiting, streaks | ✅ complete |
| Leaderboard (global + weekly, weighted by difficulty) | ✅ complete |
| Personal dashboard (heatmap, language/difficulty breakdown) | ✅ complete |
| React + Monaco editor solve page, admin panel | ✅ complete |
| Real memory-usage measurement per submission | ⚠️ stubbed (0) in local mode — Docker mode can be extended to read `docker stats` |
| Email verification, OAuth login | ❌ not built |
| Automated CI/CD pipeline | ❌ not built |

---

## 2. Prerequisites

- **JDK 17** and **Maven 3.9+** (IntelliJ bundles both)
- **Node.js 20+** and npm
- **MySQL 8** (MySQL Workbench for a GUI)
- **Postman** (or any REST client) for testing the API
- (Optional, for real sandboxing) **Docker Desktop**

---

## 3. Project Structure

```
backend/src/main/java/com/codingplatform/
├── entity/          User, Problem, TestCase, Submission, SubmissionResult, enums
├── repository/       Spring Data JPA repositories
├── dto/               Request/response DTOs
├── security/          JWT util, filter, UserDetails
├── config/            Spring Security config (CORS, stateless JWT auth)
├── service/
│   ├── execution/      CodeExecutionService (the sandbox), StreamGobbler, OutputComparator
│   ├── AuthService, ProblemService, SubmissionService, LeaderboardService, DashboardService
├── controller/        AuthController, ProblemController, SubmissionController,
│                       LeaderboardController, DashboardController, AdminController
└── exception/          Global exception handler → clean JSON error responses

frontend/src/
├── api/client.js        Axios instance with JWT interceptor
├── context/AuthContext.jsx
├── components/          Navbar, ProtectedRoute, VerdictTable, DifficultyBadge
└── pages/                Login, Register, ProblemList, ProblemSolve (Monaco editor),
                           Dashboard, Leaderboard, AdminProblems, AdminProblemForm
```

---
