# Course Delivery & Enrollment

A course delivery and enrollment system for internal training, designed to replace emailed course folders and manually maintained completion spreadsheets.

> **Assignment 05 — Course Delivery & Enrollment**

## Overview

The application provides a single place for instructors to build and manage courses, enroll learners, and monitor progress, while allowing learners to discover published courses, enroll themselves, work through lessons, and track their own progress.

The product also provides server-side course discovery, bulk enrollment, CSV progress export, an instructor dashboard, immutable course activity history, and inactivity alerts.

The assignment defines ten mandatory goals. Optional stretch features are intentionally excluded from the required scope.

## Core Features

- Email/password authentication.
- Instructor and learner roles.
- Server-enforced authorization.
- Course creation, editing, publishing, archiving, and restoration.
- Course title, description, and category.
- Lesson creation, editing, reordering, removal, and ordered display.
- Course lifecycle: `Draft → Published → Archived`.
- Learner progress lifecycle: `Not Started → In Progress → Completed`.
- Instructor enrollment and learner self-enrollment.
- Per-learner, per-course progress tracking.
- Server-side course search and discovery.
- Search by title and description.
- Filters for category, status, and instructor.
- Sorting by title, creation date, or enrollment count.
- Pagination with total match counts.
- Bulk enrollment through pasted/uploaded email lists.
- Per-address bulk enrollment results.
- CSV export of enrolled learner progress.
- Instructor dashboard with required headline metrics.
- Enrollment and progress breakdowns.
- Eight-week completion chart.
- Immutable course activity log.
- Instructor inactivity alerts with dismissal and reappearance behavior.

## Roles

### Instructor

Instructors can manage courses and lessons, publish/archive/restore courses, enroll learners, bulk-enroll learners, search and inspect all courses, view relevant progress and activity history, export course progress, and manage inactivity alerts.

### Learner

Learners can view published courses, enroll themselves, view their enrolled courses and their own progress, and work through course lessons.

Learners cannot edit course content, enroll other learners, or see other learners' progress.

All role restrictions are required to be enforced on the server.

## Course Lifecycle

```text
Draft → Published → Archived
```

A course cannot be published until it contains at least one lesson. The server must reject publication of an empty course with an explanatory message.

Archiving removes the course from the learner catalogue but does not delete its lessons or enrollment history.

## Learner Progress

```text
Not Started → In Progress → Completed
```

Progress is tracked separately for every learner/course relationship. Invalid transitions are rejected by the server.

## Course Discovery

The course list is role-aware:

- **Learners:** published courses.
- **Instructors:** drafts, published courses, and archived courses.

Discovery is performed on the server and supports:

- Text search over title and description.
- Category filter.
- Status filter.
- Instructor filter.
- Sorting by title.
- Sorting by creation date.
- Sorting by enrollment count.
- Pagination.
- Total number of matching results.

The browser must not load the complete course catalogue and perform filtering/search/sorting locally.

## Enrollment

Learners can self-enroll in published courses.

Instructors can enroll learners in published courses.

### Bulk enrollment

Instructors can paste or upload a list of email addresses. The result reports each address as:

- `Unknown address`
- `Already-enrolled learner`
- `Newly enrolled`

### Progress export

Instructors can export the progress of every learner enrolled in a course as CSV.

## Dashboard

The landing view includes:

- Total learners.
- Published courses.
- Completions this month.
- Learners currently in progress.
- Enrollment breakdown by course.
- Enrollment breakdown by progress state.
- Completion chart for the last eight weeks.

## Activity History

Each course has an immutable activity log containing:

- Course creation.
- Every course edit.
- Publish transitions.
- Archive transitions.
- Comments left by learners or the instructor.
- Who performed each action.
- When each action occurred.

History cannot be edited or deleted after the fact, including by instructors.

## Inactivity Alerts

An instructor sees an alert when a learner:

- Is `In Progress` on a course, and
- Has made no further progress for **more than 14 days**.

The instructor navigation displays an alert count badge.

An instructor can dismiss an alert for a specific learner/course combination.

If the learner subsequently engages and later becomes inactive for more than 14 days again, the alert reappears.

## Tech Stack

The assignment does not prescribe a technology stack. Use the implementation's actual stack here.

| Layer | Technology |
|---|---|
| Frontend | `<frontend technology>` |
| Backend | `<backend technology>` |
| Database | `<database technology>` |
| ORM / Data access | `<ORM or data access approach>` |
| Hosting | `<free-tier hosting provider(s)>` |

## Prerequisites

Add the actual prerequisites required by the chosen implementation.

```text
<prerequisites placeholder>
```

## Setup

Clone the public repository and install/configure the project's dependencies according to the selected stack.

```text
<setup instructions placeholder>
```

## Environment Variables

Secrets and connection details must not be committed to the repository.

Create the appropriate environment file for the chosen implementation and provide values for the variables required by the application.

Example placeholder:

```env
<ENVIRONMENT_VARIABLE_1>=<value>
<ENVIRONMENT_VARIABLE_2>=<value>
<ENVIRONMENT_VARIABLE_3>=<value>
```

Do not put real credentials, connection strings, API keys, or passwords in this README.

## Running Locally

```text
<local development command placeholder>
```

The final README should replace this placeholder with the actual frontend/backend/database startup instructions for the selected stack.

## Testing

Add the project's actual test commands here.

```text
<test command placeholder>
```

Testing should cover the mandatory assignment behavior, especially:

- Server-side role enforcement.
- Empty-course publication rejection.
- Course lifecycle transitions.
- Progress state transitions.
- Enrollment permissions.
- Server-side search/filter/sort/pagination behavior.
- Bulk enrollment result classification.
- CSV progress export.
- Dashboard calculations.
- Immutable activity history.
- Inactivity alert threshold, dismissal, and reappearance.

## Deployment

The application must be deployed to a reachable URL using free tiers.

Recommended deployment order from the assignment:

1. Provision the database.
2. Give the server its database connection details through environment variables.
3. Deploy the server and obtain its public URL.
4. Configure the browser-side application to use the server's public URL.
5. Seed sufficient demo data.
6. Verify the live application.

The assignment permits any free hosting arrangement; the suggested database/server/browser providers are examples rather than requirements.

### Live Application

`<deployed application URL>`

### Public GitHub Repository

`<public GitHub repository URL>`

### Free-tier hosting note

If the selected free-tier host sleeps while idle, document the expected wake-up delay here and in `SUBMISSION.md`.

```text
<hosting behavior placeholder>
```

## Demo Credentials

Provide demo credentials for every required role in `SUBMISSION.md`.

| Role | Email | Password |
|---|---|---|
| Instructor | `<demo instructor email>` | `<demo instructor password>` |
| Learner | `<demo learner email>` | `<demo learner password>` |

Do not commit real production credentials or secrets.

## Seed / Demo Data

The deployed application must contain enough seeded data to demonstrate the system rather than presenting an empty shell.

Document the actual seed process here:

```text
<seed instructions placeholder>
```

## Repository Documentation

The assignment requires these documents under `docs/`:

| Document | Purpose |
|---|---|
| `docs/architecture.md` | Moving pieces, communication, runtime location, representative request path, and deliberately unbuilt functionality. |
| `docs/schema.md` | Tables, columns/types, relationships, database/application constraints, denormalization, and expected first bottleneck at 100× data. |
| `docs/plan.md` | Work sessions, build order, estimates vs. actual time, and cuts made when time was short. |
| `docs/decisions.md` | At least five real decisions, rejected alternatives, reasons, and at least one later-reversed decision. |
| `docs/ai-prompts.md` | Actual AI prompts used in order, what they produced, corrections, and a bad-output example; or the no-AI process. |

The repository must also contain a completed `SUBMISSION.md`.

## Git History

The assignment explicitly requires meaningful incremental commits.

Commit work after each meaningful step rather than creating one initial commit containing the finished application. The history is part of the assessment because it demonstrates build order, changes in design, and problem-solving.

## Security and Configuration

- Keep connection strings, keys, and passwords in environment variables.
- Never commit secrets to the repository.
- Enforce role permissions on the server.
- Do not rely on UI hiding as an authorization mechanism.

## Scope

The ten mandatory assignment goals define the completion boundary.

Optional stretch ideas include:

- Quizzes with automatic scoring.
- Certificates on completion.
- Discussion threads per lesson.
- Prerequisite courses.
- Video lessons with watch-progress tracking.
- Course ratings and reviews.
- Learning paths.
- Downloadable resources per lesson.
- Email digest of inactive learners.

These are optional and do not replace any mandatory goal.

## Assignment Submission

The final submission consists of:

- Public GitHub repository URL.
- Live deployed application URL.
- Completed `SUBMISSION.md`.

`SUBMISSION.md` should also contain:

- Reviewer notes.
- Demo credentials for every role.
- Actual stack.
- Honest status of all ten goals.
- Actual time spent.
- What would be done next with another 12 hours.
- The least satisfactory part of the codebase and why.

## Development Status

```text
<project status placeholder>
```
