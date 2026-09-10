# Schema

Answer each of these, in your own words.

# 1. Table by table: what columns and types does each one have?

The schema is represented by six JPA entities: `User`, `Course`, `Lesson`, `Enrollment`, `ActivityLog`, and `InactivityAlert`

### `users`

Stores the accounts used by both instructors and learners.

| Column | Type | Constraints / meaning |
|---|---|---|
| `id` | `BIGINT` | Primary key, auto-generated with `IDENTITY` |
| `email` | `VARCHAR(255)` | `NOT NULL`, `UNIQUE`; validated as an email by the application |
| `password` | `VARCHAR(255)` | `NOT NULL`; stores the encoded/hashed password |
| `role` | `VARCHAR(255)` | `NOT NULL`; stores `INSTRUCTOR` or `LEARNER` |
| `created_at` | `DATETIME` | `NOT NULL`, set automatically when the row is created, not updated afterwards |

### `courses`

Stores the courses created by instructors.

| Column | Type | Constraints / meaning |
|---|---|---|
| `id` | `BIGINT` | Primary key, auto-generated with `IDENTITY` |
| `title` | `VARCHAR(255)` | `NOT NULL` |
| `description` | `TEXT` | `NOT NULL` |
| `category` | `VARCHAR(255)` | `NOT NULL` |
| `status` | `VARCHAR(255)` | `NOT NULL`; stores `DRAFT`, `PUBLISHED`, or `ARCHIVED` |
| `instructor_id` | `BIGINT` | `NOT NULL`, foreign key to `users.id` |
| `created_at` | `DATETIME` | `NOT NULL`, automatically set on creation |
| `updated_at` | `DATETIME` | Nullable; automatically updated when the entity changes, and explicitly updated by the service in several operations |

### `lessons`

Stores the lessons belonging to a course. In the current version, `content` stores the location of the uploaded PDF/PPT/PPTX file rather than the lesson's text itself.

| Column | Type | Constraints / meaning |
|---|---|---|
| `id` | `BIGINT` | Primary key, auto-generated with `IDENTITY` |
| `course_id` | `BIGINT` | `NOT NULL`, foreign key to `courses.id` |
| `title` | `VARCHAR(255)` | `NOT NULL` |
| `content` | `TEXT` | `NOT NULL`; currently stores the relative file path such as `/uploads/lessons/<uuid>.pdf` |
| `position` | `INT` | `NOT NULL`; ordering of the lesson inside its course |
| `created_at` | `DATETIME` | `NOT NULL`, automatically set on creation |
| `updated_at` | `DATETIME` | Nullable, automatically updated when the lesson changes |

There is a database unique constraint on `(course_id, position)`. This means two lessons in the same course cannot have the same position.

The actual uploaded file is **not** stored in this table. The file is kept in the backend's `uploads/lessons` directory during development, and only its portable relative path is stored in the database.

### `enrollments`

| Column | Type | Constraints / meaning |
|---|---|---|
| `id` | `BIGINT` | Primary key, auto-generated with `IDENTITY` |
| `learner_id` | `BIGINT` | `NOT NULL`, foreign key to `users.id` |
| `course_id` | `BIGINT` | `NOT NULL`, foreign key to `courses.id` |
| `progress_status` | `VARCHAR(255)` | `NOT NULL`; stores `NOT_STARTED`, `IN_PROGRESS`, or `COMPLETED` |
| `progress_updated_at` | `DATETIME` | `NOT NULL`; last time progress was changed |
| `enrolled_at` | `DATETIME` | `NOT NULL`, automatically set when the enrollment is created, not updated afterwards |
| `completed_at` | `DATETIME` | Nullable; populated when the enrollment reaches `COMPLETED` |

There is a database unique constraint on `(learner_id, course_id)`, so the same learner cannot have two enrollment rows for the same course.

`@PrePersist` also supplies defaults for `progress_status` and `progress_updated_at` if they were not already set.

### `activity_logs`

Stores course activity such as course creation/editing/publishing/archiving and learner/instructor comments.

| Column | Type | Constraints / meaning |
|---|---|---|
| `id` | `BIGINT` | Primary key, auto-generated with `IDENTITY` |
| `course_id` | `BIGINT` | `NOT NULL`, foreign key to `courses.id` |
| `actor_id` | `BIGINT` | `NOT NULL`, foreign key to `users.id` |
| `event_type` | `VARCHAR(255)` | `NOT NULL`; stores an `ActivityType` value such as `COURSE_CREATED` or `COMMENT` |
| `description` | `TEXT` | `NOT NULL`; description/comment text |
| `created_at` | `DATETIME` | `NOT NULL`, automatically set on creation and not updated afterwards |

### `inactivity_alerts`

Stores inactivity alerts associated with an enrollment.

| Column | Type | Constraints / meaning |
|---|---|---|
| `id` | `BIGINT` | Primary key, auto-generated with `IDENTITY` |
| `enrollment_id` | `BIGINT` | `NOT NULL`, foreign key to `enrollments.id` |
| `triggered_at` | `DATETIME` | `NOT NULL`, automatically set when the alert is created and not updated afterwards |
| `dismissed_at` | `DATETIME` | Nullable; `NULL` means the alert has not been dismissed |

The application treats an alert with a `NULL` `dismissed_at` as an active alert.

---


# 2. Which relationships are one-to-many, and which are many-to-many?


### - User → Course: one-to-many

One instructor can create many courses, while each course has one instructor.

The database represents this using `courses.instructor_id`.

### - Course → Lesson: one-to-many

One course can contain many lessons, while each lesson belongs to one course.

The JPA mapping is explicitly `Course @OneToMany` and `Lesson @ManyToOne`.

### - User ↔ Course: many-to-many through Enrollment

Learners and courses are conceptually many-to-many:

- One learner can enroll in many courses.
- One course can have many learners.

I did **not** model this as a direct JPA `@ManyToMany`. Instead, I created the `Enrollment` entity because the relationship itself has important data that is needed during the Export CSV Report for Learners:

- progress status
- progress updated time
- enrollment time
- completion time

`Enrollment` is therefore the association entity that turns the conceptual many-to-many relationship into two one-to-many relationships.

### - Course → ActivityLog: one-to-many

One course can have many activity records. Each activity belongs to one course.

### - User → ActivityLog: one-to-many

One user can be the actor for many activity records, while each activity has one actor.

### - Enrollment → InactivityAlert: one-to-many

One enrollment can have multiple inactivity-alert records over time, while each alert belongs to one enrollment.

There is no standalone many-to-many table besides the `Enrollment` association entity, because `Enrollment` itself represents the learner-course relationship.

---


# 3. Which constraints are enforced by the database, and which by application code — and why did you draw the line there?

### Database-level constraints

The JPA mappings create constraints that the database enforces no matter which application path changes or accesses the Database:

- Primary keys on every table.
- Auto-generated IDs.
- Foreign keys represented by `instructor_id`, `course_id`, `learner_id`, `actor_id`, and `enrollment_id`.
- `NOT NULL` constraints on required fields.
- Unique `users.email`.
- Unique `(learner_id, course_id)` in `enrollments`.
- Unique `(course_id, position)` in `lessons`.
- Column lengths/types such as `VARCHAR` versus `TEXT`.

Although the Application Code also checks if a learner is already enrolled or not in a course but still there can be race condition between two requests. here the `UNIQUE` Constraint at DATABASE Level acts as the final check for these cases and prevents these form happening.

### Application-level validation and rules

The service layer handles rules that depend on the current user, operation, or business state. Examples include:

- Only instructors can create, edit, manage lessons, bulk-enroll learners, or export progress for courses.
- Learners can only access published courses. 
- Learners can only change their own progress.
- A course must contain at least one lesson before it can be published.
- Only `PUBLISHED` courses can be archived.
- A selected enrollment target must actually have the `LEARNER` role.
- Progress transitions must follow `NOT_STARTED → IN_PROGRESS → COMPLETED`.

### why did you draw the line there?

Business rules change more often than basic data-integrity rules and often require context from the authenticated user or several entities. Putting those rules in the service layer keeps the database schema simpler and makes the behavior easier to test and change.

At the same time, I do not rely on Java checks for things such as uniqueness or required foreign-key relationships because they should remain true even if another code path or concurrent request writes to the database.

---


# 4. What did you deliberately denormalise?

I did not deliberately denormalise the core relational data. I kept users, courses, lessons, enrollments, activity logs, and alerts separate so that the same information is not unnecessarily duplicated.

There are two design choices that might look like denormalisation but are not really duplicated relational data:

1. **Current progress is stored on `enrollments`.** `progress_status`, `progress_updated_at`, and `completed_at` are properties of the learner-course relationship itself. They are not copies of the same data stored in another table.
2. **`lessons.content` stores a file path rather than the file contents.** This is a storage decision, not denormalisation. The binary PDF/PPT/PPTX file is kept outside MySQL and the database stores only its portable relative location.

If the system grew significantly, I could deliberately introduce denormalised read models or cached counters, but I would do that only after identifying a real read-performance bottleneck.

# 5. What would break first if this had 100x the data?

I would expect **file storage and database query volume** to become the first scaling problems, rather than the basic six-table design.

### 1. Local file storage

Lesson files are currently stored in the backend's local `uploads/lessons` directory. That works for development or one server, but it becomes a problem with multiple backend instances because each server can have a different filesystem.

I would move lesson files to shared/object storage such as S3-compatible storage and keep only the file path/key in the database.

### 2. N+1 query problems

Several parts of the application can make extra database queries for each returned course or enrollment. The course list and instructor dashboard repeatedly query enrollment counts, while enrollment and alert responses can also trigger additional course/count lookups.

At 100x the data, these repeated queries would become expensive. I would replace them with joins, grouped aggregate queries, or DTO projections so the required data is fetched in fewer database calls.

### 3. Large result sets

Activity history and enrollment lists can currently return many records at once, and CSV export loads all course enrollments into memory.

I would add pagination for UI lists and process large exports in chunks or streams.

### 4. Bulk enrollment

Bulk enrollment currently performs user and enrollment lookups inside the processing loop. For large batches, I would fetch users and existing enrollments in batches and insert new enrollments in batches.

### 5. Course search

Course search uses patterns like `lower(title) like '%search%'`. With a much larger catalog, this can become slow. I would consider MySQL full-text search or a dedicated search system if needed.

### What I would fix first

1. Move lesson files to object storage like AWS S3 before using multiple backend instances.
2. Remove the N+1 queries using joins, projections, and aggregate queries.
3. Add pagination and streaming for large result sets and exports.
4. Batch bulk enrollment operations.
5. Add Caching using tools like redis
6. Add Load Balancers and message broker like RabbitMQ if high traffic encountered
