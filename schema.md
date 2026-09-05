# Database Schema
## Assignment 05 — Course Delivery & Enrollment

> **Status:** Technical design only. No implementation is included in this document.
>
> This schema is derived from the approved BRD/PRD and the approved React + Spring Boot + MySQL + Spring Data JPA/Hibernate architecture. Where the requirements do not prescribe a physical database detail, the choice below is explicitly an implementation recommendation rather than a new product requirement.

## 1. Database Technology

- Database: MySQL
- Persistence: Spring Data JPA / Hibernate
- Relational model with foreign keys and database constraints.
- Business rules that affect state transitions and authorization remain enforced in the Spring Boot service layer.
- Database constraints provide additional integrity protection.

## 2. Core Entities

The required domain entities are:

1. `users`
2. `courses`
3. `lessons`
4. `enrollments`
5. `activity_logs`
6. `inactivity_alerts`

No separate tables are introduced for optional stretch features.

---

## 3. Entity Relationship Overview

```text
users
  │
  ├───────────────< courses
  │                    │
  │                    ├───────────────< lessons
  │                    │
  │                    ├───────────────< activity_logs
  │                    │
  │                    └───────────────< enrollments >──────── users
  │                                                   │
  │                                                   └───────< inactivity_alerts
  │
  └───────────────< activity_logs
```

Relationships:

- One instructor can own many courses.
- One course belongs to one instructor.
- One course has many lessons.
- One lesson belongs to one course.
- One learner can have many enrollments.
- One course can have many enrollments.
- One learner/course pair has one enrollment.
- One course has many activity-log entries.
- One user can be the actor for many activity-log entries.
- An enrollment can have inactivity-alert episodes.

---

# 4. `users`

Stores authenticated application users and their required roles.

| Column | Suggested MySQL type | Null | Key / Constraint | Purpose |
|---|---|---:|---|---|
| `id` | `BIGINT` | No | PK | User identifier |
| `email` | `VARCHAR(255)` | No | UNIQUE | Login and learner lookup by email |
| `password_hash` | `VARCHAR(255)` | No | | Stored password hash, never plaintext password |
| `role` | `ENUM('INSTRUCTOR','LEARNER')` | No | | Required application role |
| `created_at` | `TIMESTAMP` | No | | Account creation timestamp |

### Constraints

- `id` is the primary key.
- `email` is unique.
- `role` is limited to the two required roles.
- Password storage uses a hash; the requirements do not permit or require plaintext storage.

### Notes

The assignment requires email/password authentication and the Instructor/Learner roles. It does not require a public user-registration workflow, so registration is not part of this schema/API scope.

---

# 5. `courses`

Stores course metadata and lifecycle state.

| Column | Suggested MySQL type | Null | Key / Constraint | Purpose |
|---|---|---:|---|---|
| `id` | `BIGINT` | No | PK | Course identifier |
| `title` | `VARCHAR(255)` | No | | Course title |
| `description` | `TEXT` | No | | Course description; used by text search |
| `category` | `VARCHAR(255)` | No | INDEX recommended | Course category |
| `instructor_id` | `BIGINT` | No | FK → `users.id` | Course instructor |
| `status` | `ENUM('DRAFT','PUBLISHED','ARCHIVED')` | No | INDEX recommended | Course lifecycle state |
| `created_at` | `TIMESTAMP` | No | INDEX recommended | Creation date |
| `updated_at` | `TIMESTAMP` | No | | Last course edit timestamp |

### Constraints

- `instructor_id` references `users.id`.
- The application must ensure the referenced user has the Instructor role.
- Course state transitions are enforced in the service layer.
- A course cannot be published if it has zero lessons.

### Lifecycle

```text
DRAFT → PUBLISHED → ARCHIVED
```

Restore is supported from Archived, but the BRD/PRD does not explicitly prescribe which non-archived state restoration must target. The implementation must make this choice explicit rather than silently treating it as an assignment requirement.

---

# 6. `lessons`

Stores ordered content belonging to a course.

| Column | Suggested MySQL type | Null | Key / Constraint | Purpose |
|---|---|---:|---|---|
| `id` | `BIGINT` | No | PK | Lesson identifier |
| `course_id` | `BIGINT` | No | FK → `courses.id`, INDEX | Owning course |
| `title` | `VARCHAR(255)` | No | | Lesson title |
| `content` | `TEXT` | No | | Lesson content/description |
| `position` | `INT` | No | UNIQUE with `course_id` | Running order |
| `created_at` | `TIMESTAMP` | No | | Creation timestamp |
| `updated_at` | `TIMESTAMP` | No | | Last edit timestamp |

### Constraints

- Every lesson belongs to exactly one course.
- `course_id` references `courses.id`.
- `(course_id, position)` should be unique.
- Lesson ordering is managed by the backend.

### Ordering rule

The course lesson query should return:

```text
ORDER BY position ASC
```

The assignment requires ordered lesson display but does not prescribe the exact numeric ordering scheme.

---

# 7. `enrollments`

Represents a learner's enrollment in a course and stores that learner/course pair's progress.

| Column | Suggested MySQL type | Null | Key / Constraint | Purpose |
|---|---|---:|---|---|
| `id` | `BIGINT` | No | PK | Enrollment identifier |
| `learner_id` | `BIGINT` | No | FK → `users.id`, INDEX | Enrolled learner |
| `course_id` | `BIGINT` | No | FK → `courses.id`, INDEX | Enrolled course |
| `progress_status` | `ENUM('NOT_STARTED','IN_PROGRESS','COMPLETED')` | No | INDEX recommended | Required progress state |
| `progress_updated_at` | `TIMESTAMP` | No | INDEX recommended | Most recent progress activity used for inactivity evaluation |
| `enrolled_at` | `TIMESTAMP` | No | | Enrollment timestamp |

### Constraints

- `learner_id` references `users.id`.
- The application must ensure the referenced user has the Learner role.
- `course_id` references `courses.id`.
- `(learner_id, course_id)` must be unique.
- New enrollment starts at `NOT_STARTED`.
- Progress transitions are enforced by the backend.

### Progress lifecycle

```text
NOT_STARTED → IN_PROGRESS → COMPLETED
```

Invalid transitions must be rejected by the server.

### Why progress is on enrollment

The requirement says progress is tracked separately for every learner/course combination. Enrollment is exactly that relationship, so storing the progress state there avoids introducing a redundant progress table.

---

# 8. `activity_logs`

Append-only history for each course.

| Column | Suggested MySQL type | Null | Key / Constraint | Purpose |
|---|---|---:|---|---|
| `id` | `BIGINT` | No | PK | Activity identifier |
| `course_id` | `BIGINT` | No | FK → `courses.id`, INDEX | Course whose history changed |
| `actor_id` | `BIGINT` | No | FK → `users.id`, INDEX | User who performed the action |
| `event_type` | `ENUM(...)` | No | | Type of recorded event |
| `description` | `TEXT` | No | | Human-readable event/comment content |
| `created_at` | `TIMESTAMP` | No | INDEX | Event timestamp |

### Required event types

The physical enum should cover at least:

```text
COURSE_CREATED
COURSE_EDITED
COURSE_PUBLISHED
COURSE_ARCHIVED
COMMENT
```

These correspond to the required creation, edits, publish/archive transitions, and learner/instructor comments.

### Immutability

The application must not expose update or delete operations for activity logs.

The activity log is append-only.

Existing history must not be edited or deleted by users, including instructors.

### Transaction requirement

Course mutations that require history entries should commit the course mutation and its corresponding activity record in the same database transaction.

---

# 9. `inactivity_alerts`

Represents inactivity-alert episodes associated with a learner/course enrollment.

| Column | Suggested MySQL type | Null | Key / Constraint | Purpose |
|---|---|---:|---|---|
| `id` | `BIGINT` | No | PK | Alert episode identifier |
| `enrollment_id` | `BIGINT` | No | FK → `enrollments.id`, INDEX | Learner/course relationship |
| `triggered_at` | `TIMESTAMP` | No | | When this inactivity episode became an alert |
| `dismissed_at` | `TIMESTAMP` | Yes | | When an instructor dismissed this alert episode |

### Required behavior

An alert episode applies when the associated enrollment:

- has `progress_status = IN_PROGRESS`, and
- has had no further progress for more than 14 days.

An instructor may dismiss an alert.

A later period of more than 14 days after renewed learner engagement must be able to produce a new alert episode.

### Important modeling decision

This is deliberately modeled as an **alert episode** rather than a permanent boolean on enrollment.

That supports:

```text
In Progress
    ↓
> 14 days inactive
    ↓
Alert #1
    ↓
Dismissed
    ↓
Learner engages
    ↓
> 14 days inactive
    ↓
Alert #2
```

The exact scheduled evaluation mechanism is an implementation detail of the approved architecture.

---

# 10. Foreign-Key Summary

| Child table | Column | Parent |
|---|---|---|
| `courses` | `instructor_id` | `users.id` |
| `lessons` | `course_id` | `courses.id` |
| `enrollments` | `learner_id` | `users.id` |
| `enrollments` | `course_id` | `courses.id` |
| `activity_logs` | `course_id` | `courses.id` |
| `activity_logs` | `actor_id` | `users.id` |
| `inactivity_alerts` | `enrollment_id` | `enrollments.id` |

---

# 11. Required Uniqueness Constraints

At minimum:

```text
users.email
(lessons.course_id, lessons.position)
(enrollments.learner_id, enrollments.course_id)
```

These enforce identity, ordered lesson positions, and one learner/course enrollment.

---

# 12. Recommended Indexes

The assignment requires server-side search, filtering, sorting, pagination, dashboard aggregation, and inactivity detection. The following indexes support those required access patterns.

```text
users(email)

courses(status)
courses(category)
courses(instructor_id)
courses(created_at)

lessons(course_id, position)

enrollments(learner_id)
enrollments(course_id)
enrollments(progress_status)
enrollments(progress_updated_at)

activity_logs(course_id, created_at)
activity_logs(actor_id)
```

Composite indexes may additionally be introduced after observing the actual query plans.

The assignment does not specify an exact indexing strategy, so these are implementation recommendations rather than additional requirements.

---

# 13. Search and Sorting Data Requirements

The database must support server-side course discovery over:

- title
- description
- category
- status
- instructor
- title sorting
- creation-date sorting
- enrollment-count sorting
- pagination
- total match count

Enrollment count is derived from `enrollments` and does not need to be stored redundantly in `courses` for the initial design.

The browser must not retrieve the entire course catalogue and perform these operations locally.

---

# 14. Dashboard Data Sources

| Dashboard requirement | Primary source |
|---|---|
| Total learners | `users` filtered to Learner |
| Published courses | `courses` filtered to Published |
| Completions this month | `enrollments` where progress is Completed, based on completion time |
| Learners currently in progress | `enrollments` filtered to In Progress |
| Enrollment breakdown by course | `enrollments` grouped by course |
| Enrollment breakdown by progress state | `enrollments` grouped by progress status |
| Last eight weeks completion chart | completion events/timestamps |

## Important schema consideration

The requirement asks for **completions this month** and an **eight-week completion chart**. The current approved domain model needs a reliable completion timestamp.

Therefore, the implementation should preserve the time at which an enrollment reaches `COMPLETED`.

This can be represented by a `completed_at` timestamp on `enrollments`.

**Status:** Recommended schema addition required to support the specified dashboard metrics reliably. It is not a new business feature.

Recommended addition:

```text
enrollments.completed_at TIMESTAMP NULL
```

Rules:

- `NULL` while progress is not Completed.
- Set when the enrollment transitions to Completed.
- It should not be changed by arbitrary edits.

---

# 15. Schema-Level vs Application-Level Rules

## Database-level

Enforce where practical:

- primary keys
- foreign keys
- unique email
- unique learner/course enrollment
- unique course/lesson position
- valid enum values

## Spring Boot service-level

Enforce:

- authenticated access
- Instructor/Learner permissions
- course lifecycle transitions
- empty-course publication rule
- learner self-enrollment restrictions
- instructor enrollment restrictions
- progress transitions
- access to only a learner's own progress
- immutable activity behavior
- inactivity alert behavior
- bulk enrollment outcome classification

The BRD/PRD explicitly require server-side enforcement, so these rules must not exist only in React. 

---

# 16. Deletion Policy

The requirements explicitly say:

- archiving a course must preserve lessons and enrollment history
- activity history cannot be deleted

Therefore:

### Course

Use lifecycle status rather than physical deletion for normal course retirement.

### Activity logs

No application-level delete.

### Enrollment

Enrollment history must survive course archival.

### Lessons

Course archival must not delete lessons.

The assignment does not specify general hard-delete requirements, so physical deletion behavior should not be invented beyond what is needed for the required lesson removal operation.

---

# 17. Open Schema Decisions

These items are intentionally left as implementation decisions because the requirements do not prescribe them:

1. Exact numeric sizes for IDs.
2. Exact timestamp precision/time-zone storage strategy.
3. Exact maximum lengths beyond practical database/API constraints.
4. Exact restoration target for an archived course.
5. Exact MySQL text-search/index strategy.
6. Whether activity descriptions store structured metadata in addition to text.
7. Exact alert-evaluation scheduling interval.
8. Exact CSV column names.

These should be documented in `docs/decisions.md` once chosen during implementation.

---

# 18. Schema Boundary

The schema deliberately does **not** include tables for:

- quizzes
- certificates
- discussions
- prerequisites
- video watch tracking
- ratings/reviews
- learning paths
- downloadable resources
- email digests

Those are optional stretch features and are outside the required implementation boundary.
