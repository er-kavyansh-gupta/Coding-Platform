# CodeBench — Online Coding Practice & Skill Assessment Platform

Full-stack implementation matching your report: **Spring Boot + React + MySQL**, with a
sandboxed code execution engine (Java, Python, C++, JavaScript), problem bank with hints/editorials,
JWT auth, leaderboard, and a personal progress dashboard.

```
coding-platform/
├── backend/     Spring Boot API (Java 17, Maven)
├── frontend/    React app (Vite)
└── docker-compose.yml
```

---

## 1. What's included vs. what you should extend

This is a working, runnable MVP covering the core flows from your report. To keep it
buildable in one pass, a few things are intentionally simplified — noted below so you know
exactly what to build on for your final submission/demo:

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
| Email verification, OAuth login | ❌ not built (report lists as "nice to have") |
| Automated CI/CD pipeline | ❌ not built — manual deploy steps given in §6 |

---

## 2. Prerequisites

- **JDK 17** and **Maven 3.9+** (IntelliJ bundles both)
- **Node.js 20+** and npm
- **MySQL 8** (MySQL Workbench for a GUI)
- **Postman** (or any REST client) for testing the API
- (Optional, for real sandboxing) **Docker Desktop**

---

## 3. Run it locally — step by step

### 3.1 Database (MySQL Workbench)
1. Open MySQL Workbench, connect to your local MySQL server.
2. Nothing to create manually — `application.yml` has
   `createDatabaseIfNotExist=true`, so the `coding_platform` schema is created automatically
   on first backend startup. Tables are created by Hibernate (`ddl-auto: update`), and
   `data.sql` seeds an admin user + 3 sample problems.
3. If your MySQL root password isn't `root`, either change it to `root` for local dev, or
   set environment variables (see 3.2) before starting the backend.

### 3.2 Backend (IntelliJ IDEA)
1. `File → Open` and select the `backend/` folder (IntelliJ auto-detects the Maven project).
2. Let Maven download dependencies (bottom-right progress bar).
3. Set environment variables if your MySQL credentials differ from the defaults
   (`DB_USERNAME=root`, `DB_PASSWORD=root`) — Run/Debug Configurations → Environment variables.
4. Run `CodingPlatformApplication.java` (green ▶ next to `main`).
5. Confirm it started: open `http://localhost:8080/swagger-ui.html` — you should see the
   full API documented (register, login, problems, submissions, leaderboard, dashboard, admin).

### 3.3 Test the API (Postman)
1. `POST http://localhost:8080/api/auth/login` with body
   `{"username":"admin","password":"Admin@123"}` → copy the returned `token`.
2. For any protected endpoint, add header `Authorization: Bearer <token>`.
3. Try `GET http://localhost:8080/api/problems` to see the 3 seeded problems.
4. Try `POST http://localhost:8080/api/submissions` with:
   ```json
   {
     "problemId": 1,
     "language": "PYTHON",
     "sourceCode": "nums = list(map(int, input().split()))\ntarget = int(input())\nfor i in range(len(nums)):\n    for j in range(i+1, len(nums)):\n        if nums[i]+nums[j]==target:\n            print(i, j)\n",
     "runOnly": false
   }
   ```
   You should get back `ACCEPTED` with per-test-case results.

   > This requires `python3` on your PATH when `EXECUTION_MODE=local` (the default). For
   > Java submissions you need `javac`/`java`; for C++, `g++`; for JavaScript, `node`.

### 3.4 Frontend (VS Code)
1. Open the `frontend/` folder in VS Code.
2. `cp .env.example .env` (defaults already point at `http://localhost:8080/api`).
3. In a terminal:
   ```
   npm install
   npm run dev
   ```
4. Open `http://localhost:5173`. Log in with `admin` / `Admin@123`, or register a new account.

### 3.5 One-command alternative: Docker Compose
```
docker compose up --build
```
This builds and runs MySQL + backend + frontend together. Frontend at `http://localhost:5173`,
API at `http://localhost:8080`. `EXECUTION_MODE` defaults to `local` inside the backend
container image (which only has the JVM, not Python/g++/node) — see §5 to switch to real
Docker-sandboxed execution.

---

## 4. Project structure reference

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

## 5. About the code execution sandbox — read this before a public deploy

`app.execution.mode` in `application.yml` (or `EXECUTION_MODE` env var) has two settings:

- **`local`** (default): runs `javac`/`java`, `python3`, `g++`, `node` directly on the host
  process. Fast and simple for development, but **submitted code runs with the same
  privileges as your backend process** — do not expose this mode to untrusted users on
  the public internet.
- **`docker`**: every compile/run step spins up a short-lived, `--network=none`,
  memory- and pid-capped Docker container (`openjdk:17-slim`, `python:3.11-slim`,
  `gcc:latest`, `node:20-slim`) and destroys it immediately after. This is the isolation
  model your report describes and is what you should use for anything reachable by real
  users. To enable it:
  1. Install Docker on the host running the backend (or, if the backend itself runs in a
     container, mount `/var/run/docker.sock` into it and install the `docker` CLI in the
     backend image).
  2. `docker pull openjdk:17-slim python:3.11-slim gcc:latest node:20-slim` once, so first
     submissions aren't slow.
  3. Set `EXECUTION_MODE=docker`.

Either mode enforces **wall-clock timeouts** (`Process.waitFor(timeout)` +
`destroyForcibly()`) and per-test-case output comparison that ignores trailing whitespace.
Real memory-usage reporting isn't wired up yet in either mode — extend
`CodeExecutionService.run()` to parse `docker stats` output if you need it for the report's
"memory limit exceeded" verdict to actually trigger.

---

## 6. Making the website live (public deployment)

You don't need your own server to get a public URL. A straightforward, low-cost path:

### Option A — Railway / Render (backend + MySQL) + Vercel/Netlify (frontend)
1. **Push this project to a GitHub repo** (`git init`, commit, push).
2. **Database**: create a managed MySQL instance (Railway "MySQL" plugin, or
   PlanetScale/Aiven free tier). Note the host, port, user, password, database name.
3. **Backend**: on Railway or Render, "New Web Service" → connect your repo, root
   directory `backend/`. It will detect the `Dockerfile` and build it. Set environment
   variables: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`
   (generate a long random string), `CORS_ORIGINS=https://<your-frontend-domain>`,
   `EXECUTION_MODE=local` (Railway/Render containers can't easily run Docker-in-Docker on
   free tiers — see the note below if you need real sandboxing in production).
4. Once deployed you'll get a URL like `https://coding-platform-backend.up.railway.app`.
   Verify with `https://.../swagger-ui.html`.
5. **Frontend**: on Vercel or Netlify, "Import Project" → your repo, root directory
   `frontend/`. Build command `npm run build`, output directory `dist`. Add environment
   variable `VITE_API_BASE_URL=https://coding-platform-backend.up.railway.app/api`.
6. Deploy. You'll get a public URL like `https://codebench.vercel.app` — that's your live
   site. Update the backend's `CORS_ORIGINS` to match this exact URL and redeploy the backend.

### Option B — Single VPS (DigitalOcean/AWS EC2/Azure VM) with Docker Compose
1. Provision a small Ubuntu VM, install Docker + Docker Compose.
2. `git clone` your repo onto the VM.
3. Edit `docker-compose.yml`: set a strong `JWT_SECRET`, and if you want real sandboxing,
   change the backend service to mount `/var/run/docker.sock:/var/run/docker.sock` and
   install the `docker` CLI in `backend/Dockerfile`, then set `EXECUTION_MODE=docker`.
4. `docker compose up -d --build`.
5. Put Nginx or Caddy in front for HTTPS (Caddy auto-provisions Let's Encrypt certs with
   just a domain name pointed at the VM's IP), proxying `/` to the frontend container's
   port 80 and `/api` to the backend's port 8080.
6. Point your domain's DNS A record at the VM's IP. Your site is now live at your domain.

### Whichever option you pick
- Rotate `JWT_SECRET` and the MySQL password away from the dev defaults in this repo.
- Set `DDL_AUTO=validate` once your schema is stable, and manage schema changes with a
  migration tool (Flyway/Liquibase) instead of Hibernate auto-DDL, for anything beyond a
  demo/project submission.
- If you enable `EXECUTION_MODE=docker` for real users, also add per-user rate limiting
  at the infrastructure level (not just the app-level limiter already in `SubmissionService`)
  and monitor container resource usage — arbitrary code execution is inherently a sensitive
  surface even when sandboxed.

---

## 7. Default seeded login

| Username | Password | Role |
|---|---|---|
| `admin` | `Admin@123` | ADMIN |

Change this password (or delete/replace the seed row in `data.sql`) before any public deploy.
