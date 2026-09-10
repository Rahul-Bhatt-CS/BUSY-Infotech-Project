# Architecture

### 1. What are the moving pieces, and how do they talk to each other?

The system has three main parts:

* **Frontend** – a React/TypeScript application that provides the UI for learners and instructors.
* **Backend** – a Spring Boot REST API that handles authentication, authorization, business logic, courses, lessons, enrollments, progress, comments, and alerts.
* **Database** – MySQL stores users, courses, lessons, enrollments, activity logs, and inactivity alerts.

The frontend communicates with the backend through **HTTP/REST APIs**. The backend uses **Spring Data JPA/Hibernate** to communicate with MySQL.

Authentication is handled using **JWT**. The frontend sends the JWT with protected API requests, and the backend validates it before allowing access.

Lesson files such as PDFs and PPTs are handled separately through the backend's file-storage service.

### 2. Where does each piece run?

The **React/TypeScript frontend** is deployed on **Vercel** and communicates with the backend through HTTP/REST requests.

The **Spring Boot backend** is packaged as a Docker container and deployed on **Render**.

The **MySQL database** is hosted separately on **Aiven**. The Spring Boot backend connects to it using the database connection configured through the deployment environment.

Uploaded lesson files are currently stored in the backend's `uploads/lessons` directory.

### 3. What is the request path for one representative user action, end to end?

For example, when an instructor creates a course:

```text
Instructor
    ↓
React frontend
    ↓
HTTP POST request + JWT
    ↓
Spring Security
    ↓
Course Controller
    ↓
Course Service
    ↓
Course Repository
    ↓
MySQL
    ↓
Response
    ↓
React frontend
    ↓
Updated course shown to instructor
```

The JWT is checked first to make sure the user is authenticated and has the required instructor role. The controller receives the request, the service applies the business rules, and the repository saves the course through JPA/Hibernate.

### 4. What did you decide not to build, and why?

I focused on the requirements that were necessary for the core learning platform and did not build features that would add significant complexity without being required.

I did not build:

* A separate microservice architecture
* Real-time WebSocket communication
* A dedicated search engine
* A separate notification service
* Cloud object storage for lesson files
* A mobile application

I chose a **monolithic Spring Boot backend** because the project is small enough that keeping the components together makes development, testing, and deployment simpler. Features such as a dedicated search service or object storage can be added later if the scale requires them.

The main goal was to build a complete working system first rather than introduce infrastructure that the current scale did not need.
