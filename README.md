# LearnWithUs — Backend

Spring Boot REST API for the **LearnWithUs Course Delivery & Enrollment System**.

The backend handles authentication, role-based authorization, course and lesson management, learner enrollment and progress, bulk enrollment, activity history, inactivity alerts, dashboard data, CSV exports, and lesson file uploads.

## Features

* JWT-based authentication
* Instructor and learner roles
* Server-side role-based authorization
* Course creation, editing, publishing, archiving and restoration
* Lesson creation, editing, deletion and reordering
* PDF, PPT and PPTX lesson file uploads
* Learner self-enrollment
* Instructor enrollment
* Bulk learner enrollment by email
* Per-address bulk enrollment results
* Learner progress tracking
* Course and progress state validation
* Server-side course search, filtering, sorting and pagination
* Instructor dashboard and statistics
* Immutable course activity history
* Learner/instructor comments
* Inactivity alerts after more than 14 days without progress
* CSV export of course enrollment progress
* Health check endpoint

---

## Tech Stack

| Component           | Technology                  |
| ------------------- | --------------------------- |
| Language            | Java 21                     |
| Framework           | Spring Boot 3.4.2           |
| Build Tool          | Maven                       |
| Data Access         | Spring Data JPA / Hibernate |
| Database            | MySQL                       |
| Security            | Spring Security + JWT       |
| Password Hashing    | BCrypt                      |
| Validation          | Jakarta Bean Validation     |
| File Uploads        | Spring Multipart            |
| Containerization    | Docker                      |
| Production Backend  | Render                      |
| Production Database | Aiven MySQL                 |

---

## Project Structure

```text
src/main/java/com/BUSY/learnWithUs/
│
├── Controller/
│   ├── ActivityController.java
│   ├── AuthController.java
│   ├── BulkEnrollmentController.java
│   ├── CourseController.java
│   ├── CourseExportController.java
│   ├── DashboardController.java
│   ├── EnrollmentController.java
│   ├── HealthController.java
│   ├── InactivityAlertController.java
│   ├── LessonController.java
│   └── ProgressController.java
│
├── Dto/
│   ├── Activity/
│   ├── Alert/
│   ├── Auth/
│   ├── Bulk/
│   ├── Course/
│   ├── Dashboard/
│   ├── Enrollment/
│   ├── Lesson/
│   └── Progress/
│
├── Entity/
│   ├── User.java
│   ├── Course.java
│   ├── Lesson.java
│   ├── Enrollment.java
│   ├── ActivityLog.java
│   └── InactivityAlert.java
│
├── Repository/
│
├── Security/
│   ├── DataSeeder.java
│   ├── JwtAuthenticationFilter.java
│   ├── JwtUtils.java
│   ├── SecurityConfig.java
│   └── UserDetailsServiceImpl.java
│
└── Service/
    ├── ActivityService.java
    ├── AuthService.java
    ├── BulkEnrollmentService.java
    ├── CourseExportService.java
    ├── CourseService.java
    ├── DashboardService.java
    ├── EnrollmentService.java
    ├── FileStorageService.java
    ├── InactivityAlertService.java
    ├── LessonService.java
    └── ProgressService.java
```

The application follows a layered structure:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
MySQL
```

DTOs are used for API requests and responses rather than exposing the database entities directly.

---

## Database Model

The main tables are:

```text
users
courses
lessons
enrollments
activity_logs
inactivity_alerts
```

The important relationships are:

```text
User 1 ────────< Course

Course 1 ──────< Lesson

User 1 ────────< Enrollment >──────── 1 Course

Course 1 ──────< ActivityLog
User   1 ───────< ActivityLog

Enrollment 1 ──< InactivityAlert
```

`Enrollment` represents the many-to-many relationship between learners and courses and also stores the learner's progress state.

For the complete schema and design reasoning, see:

```text
docs/schema.md
```

---

## Authentication

Authentication uses JWT.

### Register

```http
POST /api/auth/register
```

### Login

```http
POST /api/auth/login
```

A successful login returns a JWT which the frontend sends with protected requests:

```http
Authorization: Bearer <JWT>
```

### Current user

```http
GET /api/auth/me
```

Passwords are stored using BCrypt rather than plain text.

The application uses stateless Spring Security sessions.

---

## Roles and Authorization

There are two roles:

```text
INSTRUCTOR
LEARNER
```

Authorization is enforced on the backend using Spring Security and method-level authorization.

Examples:

```text
Instructor
├── Create/update courses
├── Manage lessons
├── Publish/archive/restore courses
├── Enroll learners
├── Bulk enroll learners
├── View course enrollment/progress
├── Export progress
└── Manage inactivity alerts

Learner
├── View published courses
├── Self-enroll
├── View own enrollments
└── Update own progress
```

The frontend does not act as the security boundary.

---

## Course Lifecycle

Courses follow:

```text
Draft → Published → Archived
```

A course cannot be published without at least one lesson.

Archiving a course does not delete its lessons or enrollment history.

An archived course can be restored to `Draft`.

---

## Learner Progress

Learner progress follows:

```text
Not Started → In Progress → Completed
```

Progress is stored separately for every learner/course enrollment.

Invalid state transitions are rejected by the backend.

---

## Course Discovery

The course API supports server-side:

* Text search
* Category filtering
* Status filtering
* Instructor filtering
* Sorting by title
* Sorting by creation date
* Sorting by enrollment count
* Pagination
* Total result count

Example:

```http
GET /api/courses?page=0&size=10&search=java&sortBy=title&sortDirection=asc
```

The backend performs these operations through the database rather than loading the complete course catalogue into the browser.

---

## Enrollment

### Instructor enrollment

```http
POST /api/courses/{courseId}/enrollments
```

### Learner self-enrollment

```http
POST /api/courses/{courseId}/enroll
```

### Learner's enrollments

```http
GET /api/enrollments/my
```

### Course enrollments

```http
GET /api/courses/{courseId}/enrollments
```

---

## Bulk Enrollment

Instructors can submit a list of learner email addresses:

```http
POST /api/courses/{courseId}/enrollments/bulk
```

Each address is classified as:

```text
Unknown
Already enrolled
Newly enrolled
```

The service also validates that the matched account is a learner before creating an enrollment.

---

## Lessons

### List course lessons

```http
GET /api/courses/{courseId}/lessons
```

### Create lesson

```http
POST /api/courses/{courseId}/lessons
```

### Update lesson

```http
PUT /api/lessons/{lessonId}
```

### Delete lesson

```http
DELETE /api/lessons/{lessonId}
```

### Reorder lessons

```http
PUT /api/courses/{courseId}/lessons/reorder
```

Lessons have a position within their course and are returned in order.

---

## Lesson File Uploads

Lessons support:

```text
PDF
PPT
PPTX
```

Maximum upload size:

```text
50 MB per file
50 MB per request
```

Files are currently stored in:

```text
uploads/lessons/
```

A generated UUID is used for stored filenames, while the database stores the relative application path.

The storage logic is isolated inside:

```text
FileStorageService
```

This makes it possible to replace local storage with object storage later without changing the main lesson API.

---

## Progress

### Get enrollment progress

```http
GET /api/enrollments/{enrollmentId}/progress
```

### Update progress

```http
POST /api/enrollments/{enrollmentId}/progress
```

The backend verifies that the authenticated learner owns the enrollment before allowing progress changes.

---

## Activity History and Comments

### View course activity

```http
GET /api/courses/{courseId}/activity
```

### Add a comment

```http
POST /api/courses/{courseId}/activity/comments
```

The activity history records important course events such as:

* Course creation
* Course edits
* Publishing
* Archiving
* Comments

Activity records are treated as immutable history and do not expose update/delete operations.

---

## Inactivity Alerts

In-progress learners who have not made further progress for more than 14 days can appear in the instructor's inactivity alerts.

### List alerts

```http
GET /api/inactivity-alerts
```

### Alert count

```http
GET /api/inactivity-alerts/count
```

### Dismiss alert

```http
POST /api/inactivity-alerts/{id}/dismiss
```

A dismissed alert can become active again after the learner makes progress and subsequently becomes inactive again.

---

## Dashboard

The instructor dashboard is available through:

```http
GET /api/dashboard
```

It provides:

* Total learners
* Published courses
* Completions this month
* Learners currently in progress
* Enrollment breakdown by course
* Progress-state information
* Weekly completion data

---

## CSV Export

Instructor course progress can be exported through:

```http
GET /api/courses/{courseId}/export
```

The response contains the enrolled learners and their progress information as CSV.

---

## Health Check

The application exposes:

```http
GET /health
```

The endpoint also checks database connectivity.

A successful response is:

```text
OK
```

If the database cannot be reached, the endpoint returns:

```text
503 Service Unavailable
```

This endpoint is also used by the deployment keep-alive workflow.

---

# Local Setup

## Prerequisites

Install:

* Java 21
* Maven (optional because Maven Wrapper is included)
* MySQL 8 or another compatible MySQL server
* Git

Docker can be used instead of running Java directly.

---

## Clone the Repository

```bash
git clone https://github.com/Rahul-Bhatt-CS/BUSY-Infotech-Project.git
cd BUSY-Infotech-Project
```

---

## Database Configuration

The application reads database configuration from environment variables.

```env
DB_URL=jdbc:mysql://localhost:3306/learnwithus?createDatabaseIfNotExist=true
DB_USERNAME=root
DB_PASSWORD=your_password
```

If these variables are not provided, the application falls back to the local development defaults defined in `application.properties`.

---

## JWT Configuration

Set a strong JWT secret through:

```env
JWT_SECRET=your-secure-secret
```

The secret should be at least 256 bits in production.

JWT expiration is configured in milliseconds:

```env
JWT_EXPIRATION=86400000
```

> The current application defines the expiration value in `application.properties`. Keep production secrets outside the repository.

---

## CORS Configuration

The backend accepts requests from the configured frontend URL.

Set:

```env
FRONTEND_URL=http://localhost:5173
```

For production, this should point to the deployed frontend URL.

---

## Run with Maven

On Windows:

```bash
mvnw.cmd spring-boot:run
```

On Linux/macOS:

```bash
./mvnw spring-boot:run
```

The server starts on:

```text
http://localhost:8080
```

The port can be changed with:

```env
PORT=8080
```

---

## Build

```bash
./mvnw clean package
```

On Windows:

```bash
mvnw.cmd clean package
```

The generated JAR is placed under:

```text
target/
```

---

## Run the JAR

```bash
java -jar target/learnWithUs-0.0.1-SNAPSHOT.jar
```

---

# Docker

The project includes a multi-stage Dockerfile using Java 21.

Build:

```bash
docker build -t learnwithus-backend .
```

Run:

```bash
docker run -p 8080:8080 \
  -e DB_URL="your-database-url" \
  -e DB_USERNAME="your-database-user" \
  -e DB_PASSWORD="your-database-password" \
  -e JWT_SECRET="your-jwt-secret" \
  -e FRONTEND_URL="http://localhost:5173" \
  learnwithus-backend
```

The container exposes port:

```text
8080
```

In deployment environments such as Render, the application uses the platform-provided `PORT` value automatically.

---

# Demo Users

The application seeds demo users when it starts.

### Instructor

```text
Email:    instructor@learnwithus.com
Password: Instructor@123
Role:     INSTRUCTOR
```

### Learner

```text
Email:    learner@learnwithus.com
Password: Learner@123
Role:     LEARNER
```

Additional learner accounts are also seeded:

```text
learner1@learnwithus.com
learner2@learnwithus.com
learner3@learnwithus.com
...
learner9@learnwithus.com
```

All use:

```text
Learner@123
```

The seeder checks whether each email already exists before creating the account, so restarting the application does not create duplicate users.

---

# Configuration

Important application properties include:

| Variable             | Purpose                       |
| -------------------- | ----------------------------- |
| `PORT`               | Server port                   |
| `DB_URL`             | MySQL JDBC URL                |
| `DB_USERNAME`        | Database username             |
| `DB_PASSWORD`        | Database password             |
| `JWT_SECRET`         | JWT signing secret            |
| `FRONTEND_URL`       | Allowed frontend origin       |
| `SHOW_SQL`           | Enable SQL logging            |
| `FORMAT_SQL`         | Format Hibernate SQL          |
| `SECURITY_LOG_LEVEL` | Spring Security logging level |

File upload limits are currently:

```text
Max file size:    50 MB
Max request size: 50 MB
```

---

# Production Deployment

The current application is deployed using:

```text
Frontend → Vercel
Backend  → Render
Database → Aiven MySQL
```

The backend is packaged as a Docker container and deployed to Render.

The production backend health endpoint is:

```text
https://busy-infotech-project.onrender.com/health
```

The frontend communicates with the backend through the deployed API URL.

---

## Aiven Database Note

The Aiven MySQL instance may occasionally become unavailable and require a manual restart.

If the application continues returning database errors after waiting for some time, the database should be checked/restarted before treating the backend itself as unavailable.

---

# API Documentation

The complete API specification is maintained separately in:

```text
api-spec.md
```

It documents the API endpoints, request/response structures and expected behavior.

---

# Design Documentation

The repository also contains design and development documentation:

```text
docs/
├── architecture.md
├── schema.md
├── plan.md
├── decisions.md
└── ai-prompts.md
```

These documents explain the architecture, database design, development process, technical decisions and AI-assisted development process.

---

# Development Approach

The backend was built incrementally rather than as one large implementation.

The main progression was:

```text
Project setup
      ↓
Schema and entities
      ↓
JWT security
      ↓
Course APIs
      ↓
Business logic
      ↓
Role authorization
      ↓
Lessons and files
      ↓
Enrollment and bulk enrollment
      ↓
Dashboard / activity / alerts
      ↓
Deployment
      ↓
Bug fixes and deployment reliability
```

The Git history contains the individual implementation and debugging steps.

---

# Current Limitations

The current implementation is intentionally focused on the assignment requirements.

Some areas that could be improved for a larger production system include:

* Replacing local lesson-file storage with object storage.
* Removing remaining N+1 database query patterns.
* Adding pagination to currently unbounded history/enrollment responses.
* More extensive automated tests.
* Batch processing for very large bulk-enrollment requests.
* Full-text search for a much larger course catalogue.
* More comprehensive monitoring and observability.

These are documented in more detail in `docs/schema.md` and `docs/decisions.md`.

---

# Related Repository

Frontend:

```text
https://github.com/Rahul-Bhatt-CS/BUSY-Infotech-Project-Frontend
```

Live application:

```text
https://busy-infotech-project-frontend.vercel.app/
```

Backend:

```text
https://github.com/Rahul-Bhatt-CS/BUSY-Infotech-Project
```
