# AI prompts

The prompts actually used, in the order they were used, grouped by what I was trying to achieve. For each significant one: what I asked, what I got back, and what I had to correct.

## 1. What you were trying to achieve

With the first prompt, I wanted to extract the critical information from the provided ZIP file and organize it into well-structured documents (`BRD.md`, `PRD.md`, and `README.md`). The goal was to convert the assignment requirements into clear documentation that could later be used as implementation context.

### Prompt

> Read the entire provided ZIP and every file inside it carefully.
>
> Based ONLY on the assignment requirements in the ZIP, create these 3 files:
>
> 1. BRD.md — Business Requirements Document
> 2. PRD.md — Product Requirements Document
> 3. README.md — professional project README
>
> BRD: business problem, objectives, stakeholders, users/roles, scope, business rules, and success criteria.
>
> PRD: complete functional requirements, user stories, user flows, permissions, edge cases, validations, acceptance criteria, and all mandatory assignment requirements.
>
> README: project overview, features, roles, tech stack placeholder, setup placeholder, environment variables placeholder, running instructions placeholder, testing, deployment, and demo credentials placeholder.
>
> Do NOT invent requirements that aren't supported by the assignment.
> Keep the documents professional, clear, implementation-ready, and suitable as context for an AI coding agent.
>
> Do not write application code yet.

### What you got

The AI produced three structured documents containing the main requirements extracted from the assignment:

* `BRD.md` — business problem, objectives, stakeholders, users/roles, scope, business rules, and success criteria.
* `PRD.md` — functional requirements, user stories, flows, permissions, validations, edge cases, acceptance criteria, and mandatory assignment requirements.
* `README.md` — project overview, features, roles, technology placeholders, setup placeholders, environment variables, testing, deployment, and demo credential placeholders.

These documents became the baseline requirements for the subsequent architecture and implementation work.

### What you corrected

I reviewed the generated documents against the original assignment rather than accepting the AI output blindly.

The main correction was to ensure that requirements remained within the assignment's scope. In particular, optional stretch features were kept explicitly separate from the ten mandatory goals, and unsupported business requirements were not treated as mandatory functionality.

I also retained placeholders in the README for implementation-specific information that was not yet known, such as the final hosting provider, setup commands, environment variables, live URL, and demo credentials.

---

## 2. What you were trying to achieve

The next prompt was used to turn the approved requirements into a technical architecture before implementation. The objective was to decide how the frontend, backend, database, authentication, domain modules, and deployment pieces would communicate.

### Prompt

> Based on the BRD, PRD, and README, analyze the requirements and propose the complete technical architecture. Do not write code yet.

### What you got

The AI proposed a modular-monolith architecture using:

* React for the frontend.
* Spring Boot for the backend.
* MySQL for persistence.
* Spring Data JPA/Hibernate for data access.
* Spring Security for authentication and authorization.
* REST APIs between frontend and backend.
* Domain-oriented backend modules for authentication, users, courses, lessons, enrollment, progress, activity history, alerts, dashboard, and export.
* Server-side enforcement of business rules.
* Server-side search, filtering, sorting, and pagination.
* Backend-generated CSV exports.
* Backend-generated dashboard aggregations.

The architecture also described the expected request flow from the React frontend through Spring Boot services/repositories to MySQL.

### What you corrected

The architecture was kept within the mandatory assignment scope. Optional features were deliberately excluded from the initial architecture so they would not consume implementation time before the ten required goals were complete.

Some implementation choices were treated as technical decisions rather than assignment requirements. For example, JWT authentication and the exact backend module structure were architecture decisions, not requirements stated literally by the assignment.

---

## 3. What you were trying to achieve

The next prompt was used to convert the approved architecture and requirements into a concrete database schema and API contract before implementation.

### Prompt

> Now create the database schema and API specification based strictly on the approved requirements and architecture. Do not implement anything yet.

### What you got

The AI produced:

* `docs/schema.md`
* `docs/api-spec.md`

The schema defined the main persistence entities for:

* Users.
* Courses.
* Lessons.
* Enrollments.
* Activity logs.
* Inactivity alerts.

It also documented relationships, constraints, indexes, state transitions, and which rules belong at the database layer versus the application/service layer.

The API specification defined the proposed REST endpoints for authentication, courses, lessons, enrollment, progress, activity history, inactivity alerts, dashboard data, and CSV export.

### What you corrected

The assignment specifies required behavior but does not prescribe literal API URLs or database table names. Therefore, the API paths and schema details were treated as proposed technical contracts rather than being presented as requirements from the assignment.

A schema consideration was also documented for completion timestamps so that the required dashboard metrics, particularly monthly and eight-week completion reporting, could be calculated reliably.

---

## 4. What you were trying to achieve

The next prompt was used to define the complete UI/UX behavior before implementing the frontend.

### Prompt

> Create the complete UI/UX specification based on the PRD. Define every screen, navigation flow, role-specific behavior, loading/empty/error states, forms, tables, filters and dashboards. Do not write code.

### What you got

The AI produced `docs/ui-ux-spec.md`.

It defined:

* Authentication screen.
* Instructor dashboard.
* Course catalogue.
* My Courses.
* Course learning view.
* Course creation/editing.
* Course management.
* Lesson management.
* Single and bulk enrollment.
* Learner progress view.
* Activity history.
* Inactivity alerts.
* Role-specific navigation.
* Forms, tables, filters, sorting, pagination.
* Loading, empty, error, and success states.
* Navigation flows.
* UI-level security behavior.


---

## 5. What you were trying to achieve

The next prompt was used to implement the frontend based on the approved API specification and UI/UX specification. The goal was to create the React application incrementally while keeping the implementation aligned with the documented requirements and API contracts.

### Prompt

> Using the API specification and UI/UX specification, initialize the project and implement it incrementally. Do not make architectural decisions without documenting them.

### What you got

The AI produced a ZIP file containing the frontend implementation, including the React pages, components, API integration, authentication flow, course and lesson interfaces, learner progress views, instructor functionality, and supporting TypeScript types.

### What you corrected

The generated code required several rounds of review and correction before it matched the actual project requirements and backend API.

1. The generated code was not consistently formatted or human-readable, so the project files were reformatted using **Prettier**.
2. Several frontend API types did not match the DTOs returned by the backend. This caused API response and component errors, so the TypeScript types in `src/types/index.ts` were updated to match the actual backend DTO structure.
3. Additional debugging was performed across the frontend and backend when API responses, component expectations, and backend DTOs did not initially align.
4. Backend issues such as incorrect JPQL entity attributes, CORS configuration, and authentication/authorization behavior were debugged iteratively rather than assuming the initial generated implementation was correct.

---

## 6. What you were trying to achieve

The original lesson implementation stored lesson content as a string that the instructor had to enter manually. The requirement later evolved so that a lesson could contain actual learning material in the form of a **PDF, PPT, or PPTX file**.

The goal of this prompt was to modify the existing lesson implementation without redesigning the database unnecessarily. The existing `Lesson.content` string would instead store the location of the uploaded file, while the actual file would be stored in the backend's uploads directory during development. The design would also allow the storage mechanism to be replaced with cloud storage such as AWS S3 in production.

### Prompt

> Update the backend and frontend code so that we can send PPT or PDF as lessons. The database already stores a string as the lesson content; instead, now it will store the location of the file and the file will be stored in the backend. Use the backend uploads folder for now, but for production it will store a link to cloud storage like AWS S3.

### What you got

The AI implemented the initial file-storage functionality by:

* Creating `FileStorageService.java` to handle storing uploaded files.
* Modifying `LessonService.java` to process uploaded lesson files.
* Modifying `LessonController.java` to accept file uploads.
* Updating `application.properties` for multipart file-upload configuration.
* Extending the frontend lesson form to allow instructors to select lesson files.
* Updating the frontend API calls to send lesson data using `multipart/form-data`.
* Keeping the existing `Lesson.content` database field as a `String`, but changing its purpose so that it stores the file location instead of the lesson text.
* Supporting PDF, PPT, and PPTX lesson materials.
* Storing files locally in the backend `uploads` directory during development.

The storage logic was kept separate from the lesson business logic so that it can later be replaced with an AWS S3-based implementation without requiring a major redesign of the lesson system.

### What you corrected

The initial implementation was reviewed against the existing project rather than being accepted as-is.

The main correction was ensuring that the new file-upload functionality remained compatible with the existing lesson, course, progress, and authentication flows. In particular:

1. The existing database field was retained as a `String` instead of introducing a new binary/BLOB column.
2. File locations were stored as portable paths rather than machine-specific absolute filesystem paths.
3. The frontend and backend API contracts were updated together because lesson creation was no longer a normal JSON request; it now required `multipart/form-data`.
4. File types were restricted to the required lesson formats rather than accepting arbitrary files.
5. The implementation was designed with future AWS S3 storage in mind, so local filesystem storage can later be replaced without changing the lesson domain model.
