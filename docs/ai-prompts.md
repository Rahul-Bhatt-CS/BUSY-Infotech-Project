# AI prompts

The prompts actually used, in the order they were used, grouped by what I was trying to achieve. For each significant one: what I asked, what I got back, and what I had to correct.

## What you were trying to achieve

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

## What you were trying to achieve

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

## What you were trying to achieve

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

## What you were trying to achieve

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

### What you corrected

first developing backend then will start with the frontend review and implementation

---

## What you were trying to achieve

The next prompt was used to define the complete UI/UX behavior and implementing the frontend.

### Prompt

> Using the BRD, PRD, README, approved architecture, database schema, API specification and UI/UX specification, initialize the project and implement it incrementally. Do not make architectural decisions without documenting them.

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

### What you corrected

first developing backend then will start with the frontend review and implementation