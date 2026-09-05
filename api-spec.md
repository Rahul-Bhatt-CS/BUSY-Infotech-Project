# API Specification
## Assignment 05 — Course Delivery & Enrollment

> **Status:** API design only. No implementation is included.
>
> The endpoint names and payload shapes in this document are proposed technical contracts derived from the approved requirements and architecture. The assignment specifies behavior and permissions, but does not prescribe literal URL names.

## 1. API Principles

- Protocol: HTTPS in deployed environments.
- Style: REST-oriented HTTP API.
- Backend: Spring Boot.
- Authentication: email/password with the approved Spring Security architecture.
- Authorization: server-side.
- Data exchange: JSON for normal API requests/responses.
- CSV: `text/csv` for progress export.
- Pagination/search/filter/sort: performed by the server.
- Business-rule violations return an explicit error rather than being silently corrected.
- Activity history has no update/delete API.

Base path:

```text
/api
```

---

# 2. Authentication

## 2.1 Login

```http
POST /api/auth/login
```

### Purpose

Authenticate an Instructor or Learner with email/password.

### Request

```json
{
  "email": "user@example.com",
  "password": "..."
}
```

### Success

```text
200 OK
```

```json
{
  "token": "<authentication-token>",
  "user": {
    "id": 1,
    "email": "user@example.com",
    "role": "INSTRUCTOR"
  }
}
```

The exact token format is an implementation choice within the approved security architecture.

### Errors

```text
401 Unauthorized
```

for invalid credentials.

---

## 2.2 Current User

```http
GET /api/auth/me
```

### Authentication

Required.

### Purpose

Return the currently authenticated user and role for frontend session initialization.

### Success

```text
200 OK
```

```json
{
  "id": 1,
  "email": "user@example.com",
  "role": "INSTRUCTOR"
}
```

---

# 3. Courses

## 3.1 List/Search Courses

```http
GET /api/courses
```

### Authentication

Required.

### Permissions

- Learner: published courses.
- Instructor: draft, published, and archived courses.

### Query parameters

```text
search
category
status
instructorId
sortBy
sortDirection
page
size
```

### Supported `sortBy`

```text
title
createdAt
enrollmentCount
```

### Example

```text
GET /api/courses?search=spring&category=Backend&status=PUBLISHED&sortBy=title&sortDirection=asc&page=0&size=10
```

### Required behavior

Search must operate over title and description.

Filtering must support:

- category
- status
- instructor

Sorting must support:

- title
- creation date
- enrollment count

Pagination and total matching results must be returned by the server.

### Response

```json
{
  "items": [
    {
      "id": 42,
      "title": "Spring Boot",
      "description": "Backend fundamentals",
      "category": "Backend",
      "status": "PUBLISHED",
      "instructor": {
        "id": 1,
        "email": "instructor@example.com"
      },
      "enrollmentCount": 12,
      "createdAt": "2026-09-01T10:00:00Z"
    }
  ],
  "page": 0,
  "size": 10,
  "totalMatches": 1
}
```

`enrollmentCount` is included because sorting/reporting by enrollment count is required.

---

## 3.2 Get Course

```http
GET /api/courses/{courseId}
```

### Authentication

Required.

### Permissions

- Learner: only if the course is published and otherwise accessible to the learner.
- Instructor: course management/access according to instructor permissions.

### Response

```json
{
  "id": 42,
  "title": "Spring Boot",
  "description": "Backend fundamentals",
  "category": "Backend",
  "status": "PUBLISHED",
  "instructor": {
    "id": 1,
    "email": "instructor@example.com"
  },
  "lessons": [
    {
      "id": 100,
      "title": "Introduction",
      "content": "...",
      "position": 1
    }
  ]
}
```

Lessons must be returned in course order.

---

# 4. Course Management

## 4.1 Create Course

```http
POST /api/courses
```

### Role

Instructor only.

### Request

```json
{
  "title": "Spring Boot",
  "description": "Backend fundamentals",
  "category": "Backend"
}
```

### Success

```text
201 Created
```

New course status:

```text
DRAFT
```

A `COURSE_CREATED` activity entry must be recorded.

---

## 4.2 Edit Course

```http
PUT /api/courses/{courseId}
```

### Role

Instructor only.

### Request

```json
{
  "title": "Updated title",
  "description": "Updated description",
  "category": "Backend"
}
```

### Success

```text
200 OK
```

Every edit must create a `COURSE_EDITED` activity entry.

---

## 4.3 Publish Course

```http
POST /api/courses/{courseId}/publish
```

### Role

Instructor only.

### Server validations

1. Course exists.
2. Caller has Instructor permission.
3. Current course state permits the transition.
4. Course contains at least one lesson.

### Empty-course behavior

If there are zero lessons:

```text
400 Bad Request
```

Example:

```json
{
  "code": "COURSE_EMPTY",
  "message": "A course must contain at least one lesson before it can be published."
}
```

The course remains Draft.

### Success

```text
200 OK
```

The course becomes:

```text
PUBLISHED
```

and a `COURSE_PUBLISHED` activity entry is recorded.

---

## 4.4 Archive Course

```http
POST /api/courses/{courseId}/archive
```

### Role

Instructor only.

### Success

Course becomes:

```text
ARCHIVED
```

and a `COURSE_ARCHIVED` activity entry is recorded.

Lessons and enrollment history remain.

Archived courses are excluded from the learner catalogue.

---

## 4.5 Restore Course

```http
POST /api/courses/{courseId}/restore
```

### Role

Instructor only.

### Behavior

Restores an archived course without deleting lessons or enrollment history.

The exact target status must be selected as an explicit implementation decision because the approved requirements do not specify whether restoration always returns to Draft or Published.

---

# 5. Lessons

## 5.1 List Course Lessons

```http
GET /api/courses/{courseId}/lessons
```

### Authentication

Required.

### Behavior

Return lessons in their defined course order.

---

## 5.2 Add Lesson

```http
POST /api/courses/{courseId}/lessons
```

### Role

Instructor only.

### Request

```json
{
  "title": "Introduction",
  "content": "Lesson content",
  "position": 1
}
```

### Behavior

The lesson belongs to exactly one course.

The backend must preserve unambiguous lesson ordering.

---

## 5.3 Edit Lesson

```http
PUT /api/lessons/{lessonId}
```

### Role

Instructor only.

### Request

```json
{
  "title": "Updated lesson",
  "content": "Updated content",
  "position": 2
}
```

---

## 5.4 Remove Lesson

```http
DELETE /api/lessons/{lessonId}
```

### Role

Instructor only.

### Behavior

Removes the lesson from the course while preserving unrelated enrollment history.

The backend must maintain valid lesson ordering after removal.

---

## 5.5 Reorder Lessons

```http
PUT /api/courses/{courseId}/lessons/reorder
```

### Role

Instructor only.

### Request

```json
{
  "lessonIds": [105, 101, 109]
}
```

### Behavior

The supplied lesson ordering becomes the course's running order.

The server must ensure the lessons belong to the specified course and the resulting positions are unambiguous.

---

# 6. Enrollment

## 6.1 Self Enrollment

```http
POST /api/courses/{courseId}/enroll
```

### Role

Learner only.

### Validations

- Course exists.
- Course is Published.
- Caller is a Learner.
- Caller is not already enrolled.

### Success

```text
201 Created
```

The enrollment starts with:

```text
NOT_STARTED
```

---

## 6.2 Instructor Enrollment

```http
POST /api/courses/{courseId}/enrollments
```

### Role

Instructor only.

### Request

```json
{
  "learnerId": 25
}
```

### Validations

- Course exists.
- Course is Published.
- Caller is an Instructor.
- Target user is a Learner.
- Enrollment does not already exist.

### Success

```text
201 Created
```

---

## 6.3 My Enrolled Courses

```http
GET /api/enrollments/my
```

### Role

Learner.

### Purpose

Return all courses in which the authenticated learner is enrolled, with progress for each.

### Response

```json
{
  "items": [
    {
      "enrollmentId": 500,
      "course": {
        "id": 42,
        "title": "Spring Boot"
      },
      "progressStatus": "IN_PROGRESS"
    }
  ]
}
```

---

## 6.4 Instructor Course Enrollments

```http
GET /api/courses/{courseId}/enrollments
```

### Role

Instructor only.

### Purpose

Provide instructor access to enrolled learner/progress information required for course oversight and CSV export.

The exact pagination behavior for this endpoint is not prescribed by the assignment and should only be added if useful to the implementation.

---

# 7. Bulk Enrollment

## 7.1 Bulk Enrollment

```http
POST /api/courses/{courseId}/enrollments/bulk
```

### Role

Instructor only.

### Input

The API must support the assignment's two input modes:

- pasted email addresses
- uploaded email list

A JSON form can support pasted addresses:

```json
{
  "emails": [
    "learner1@example.com",
    "learner2@example.com",
    "unknown@example.com"
  ]
}
```

An upload form may use `multipart/form-data` for a file.

### Result

Every submitted address receives an independent result.

```json
{
  "results": [
    {
      "email": "learner1@example.com",
      "result": "NEWLY_ENROLLED"
    },
    {
      "email": "learner2@example.com",
      "result": "ALREADY_ENROLLED"
    },
    {
      "email": "unknown@example.com",
      "result": "UNKNOWN_ADDRESS"
    }
  ]
}
```

### Required result categories

```text
UNKNOWN_ADDRESS
ALREADY_ENROLLED
NEWLY_ENROLLED
```

A failure for one address must not prevent results from being reported for the other addresses.

---

# 8. Progress

## 8.1 Get Own Enrollment Progress

```http
GET /api/enrollments/{enrollmentId}/progress
```

### Role

Learner, only for their own enrollment.

### Server authorization

The server must verify:

```text
enrollment.learnerId == authenticatedUser.id
```

A learner must not be able to retrieve another learner's progress.

---

## 8.2 Change Progress

```http
POST /api/enrollments/{enrollmentId}/progress
```

### Role

Learner, only for their own enrollment.

### Request

```json
{
  "status": "IN_PROGRESS"
}
```

or:

```json
{
  "status": "COMPLETED"
}
```

### Valid transitions

```text
NOT_STARTED → IN_PROGRESS
IN_PROGRESS → COMPLETED
```

### Invalid transition

The server rejects:

```text
COMPLETED → IN_PROGRESS
COMPLETED → NOT_STARTED
NOT_STARTED → COMPLETED
```

with an appropriate client/business-rule error.

### Side effects

On valid progress activity:

- update progress state
- update `progressUpdatedAt`
- if transitioning to Completed, record the completion timestamp
- make subsequent inactivity evaluation use the new progress activity time

The exact activity-log event for progress changes is not explicitly required by the assignment, so it must not be invented as a mandatory activity event.

---

# 9. Activity History

## 9.1 Get Course Activity

```http
GET /api/courses/{courseId}/activity
```

### Authentication

Required.

### Permissions

Only users who can access the course may view its activity history.

### Response

```json
{
  "items": [
    {
      "id": 1,
      "eventType": "COURSE_CREATED",
      "description": "Course created",
      "actor": {
        "id": 1,
        "email": "instructor@example.com"
      },
      "createdAt": "2026-09-01T10:00:00Z"
    }
  ]
}
```

---

## 9.2 Add Comment

```http
POST /api/courses/{courseId}/activity/comments
```

### Role

Learner or Instructor, subject to course access.

### Request

```json
{
  "comment": "Completed the lesson."
}
```

### Behavior

Creates a new immutable `COMMENT` activity entry containing:

- course
- actor
- comment
- timestamp

---

## 9.3 Activity Modification

There are intentionally **no** endpoints for:

```text
PUT  /api/activity/{id}
PATCH /api/activity/{id}
DELETE /api/activity/{id}
```

This is required to preserve immutable history.

---

# 10. Inactivity Alerts

## 10.1 List Instructor Alerts

```http
GET /api/inactivity-alerts
```

### Role

Instructor only.

### Purpose

Return currently relevant inactivity alerts.

An alert is associated with an enrollment where:

```text
progressStatus = IN_PROGRESS
AND
no further progress > 14 days
```

---

## 10.2 Alert Count

```http
GET /api/inactivity-alerts/count
```

### Role

Instructor only.

### Response

```json
{
  "count": 3
}
```

This supports the instructor navigation badge.

The count should represent the currently active/dismissible alerts according to the application's alert state.

---

## 10.3 Dismiss Alert

```http
POST /api/inactivity-alerts/{alertId}/dismiss
```

### Role

Instructor only.

### Behavior

Marks the specific alert episode as dismissed.

Dismissal must not permanently suppress future inactivity after learner engagement.

---

## 10.4 Alert Reappearance

No separate "reappear" endpoint is required.

Instead:

```text
learner engages
        ↓
progressUpdatedAt changes
        ↓
previous inactivity episode ends
        ↓
later >14-day inactivity
        ↓
new alert episode
```

The backend's inactivity evaluation creates the new alert episode.

---

# 11. Dashboard

## 11.1 Dashboard Summary

```http
GET /api/dashboard
```

### Role

Instructor only.

### Response

```json
{
  "headlineMetrics": {
    "totalLearners": 100,
    "publishedCourses": 12,
    "completionsThisMonth": 24,
    "learnersInProgress": 31
  },
  "enrollmentByCourse": [
    {
      "courseId": 42,
      "courseTitle": "Spring Boot",
      "enrollmentCount": 25
    }
  ],
  "enrollmentByProgress": [
    {
      "status": "NOT_STARTED",
      "count": 20
    },
    {
      "status": "IN_PROGRESS",
      "count": 31
    },
    {
      "status": "COMPLETED",
      "count": 49
    }
  ],
  "weeklyCompletions": [
    {
      "week": "2026-07-13",
      "count": 4
    }
  ]
}
```

### Required metrics

The endpoint must provide:

- total learners
- published courses
- completions this month
- learners currently in progress
- enrollment by course
- enrollment by progress state
- completion data for the last eight weeks

---

# 12. CSV Progress Export

## 12.1 Export Course Progress

```http
GET /api/courses/{courseId}/progress/export
```

### Role

Instructor only.

### Response

```text
200 OK
Content-Type: text/csv
Content-Disposition: attachment; filename="course-progress.csv"
```

The CSV must contain the progress of **every learner enrolled in the selected course**.

The exact CSV columns are not prescribed by the assignment and should be finalized as an implementation decision.

At minimum, the export needs enough information to identify each enrolled learner and their course progress.

---

# 13. Standard Authorization Matrix

| Endpoint/function | Instructor | Learner |
|---|---:|---:|
| Login | Yes | Yes |
| View accessible courses | Yes | Yes |
| Create course | Yes | No |
| Edit course | Yes | No |
| Publish | Yes | No |
| Archive | Yes | No |
| Restore | Yes | No |
| Manage lessons | Yes | No |
| Self-enroll | Not required | Yes |
| Enroll learner | Yes | No |
| Bulk enroll | Yes | No |
| View own progress | Yes, where applicable | Yes |
| View other learner progress | Yes | No |
| Add activity comment | Yes | Yes |
| Edit/delete activity | No | No |
| Export progress CSV | Yes | No |
| Dashboard | Yes | No |
| View inactivity alerts | Yes | No |
| Dismiss inactivity alert | Yes | No |

The permission model is based directly on the approved PRD. 

---

# 14. Error Contract

Use one consistent API error structure.

Recommended shape:

```json
{
  "status": 400,
  "code": "BUSINESS_RULE_VIOLATION",
  "message": "Human-readable explanation"
}
```

This is a technical convention, not a new product requirement.

## Important errors

### Unauthenticated

```text
401 Unauthorized
```

### Authenticated but forbidden

```text
403 Forbidden
```

Examples:

- learner tries to modify a course
- learner tries to enroll another learner
- learner tries to access another learner's progress

### Resource missing

```text
404 Not Found
```

### Invalid business/state operation

```text
400 Bad Request
```

or `409 Conflict` if the implementation chooses to represent a state conflict that way.

Examples:

- publish empty course
- invalid course transition
- invalid progress transition
- duplicate enrollment

### Empty course publication

Recommended:

```json
{
  "status": 400,
  "code": "COURSE_EMPTY",
  "message": "A course must contain at least one lesson before it can be published."
}
```

The explanatory message is explicitly required by the PRD.

---

# 15. Server-Side Enforcement Requirements

The following checks must occur on the backend even if the frontend already performs equivalent UI validation:

### Course

- caller is authorized
- course exists
- lifecycle transition is valid
- publishing requires at least one lesson

### Lesson

- caller is an Instructor
- lesson belongs to requested course
- ordering remains valid

### Enrollment

- learner self-enrollment is for the authenticated learner
- instructor enrollment targets a learner
- course is Published
- duplicate enrollment is rejected/classified appropriately

### Progress

- caller owns the enrollment when caller is a Learner
- transition is valid

### Activity

- viewer has course access
- new comments record the authenticated actor
- existing activity cannot be edited/deleted

### Export

- caller is an Instructor
- requested course exists
- returned data belongs to the requested course

These are required consequences of the approved server-side authorization and validation requirements. 

---

# 16. Request Flow

## Example: Publish

```text
React
  │
  │ POST /api/courses/42/publish
  ▼
Spring Security
  │
  ├── authenticate
  └── authorize Instructor
  │
  ▼
CourseController
  │
  ▼
CourseService
  │
  ├── load course
  ├── validate lifecycle
  ├── verify lesson count > 0
  ├── change status
  └── record activity
  │
  ▼
MySQL transaction
  │
  ├── UPDATE courses
  └── INSERT activity_logs
  │
  ▼
HTTP response
```

---

# 17. Request Flow: Learner Progress

```text
React
  │
  │ POST /api/enrollments/500/progress
  ▼
Spring Security
  │
  ▼
ProgressController
  │
  ▼
ProgressService
  │
  ├── load enrollment
  ├── verify authenticated learner owns it
  ├── validate state transition
  ├── update progress
  └── update progress timestamp
  │
  ▼
MySQL transaction
  │
  ▼
Response
```

---

# 18. Request Flow: Course Search

```text
React
  │
  │ GET /api/courses?search=java&status=PUBLISHED&page=0&size=10
  ▼
CourseController
  │
  ▼
CourseService
  │
  ▼
CourseRepository
  │
  ├── WHERE search/filter conditions
  ├── ORDER BY requested sort
  ├── LIMIT/OFFSET
  └── COUNT matching rows
  │
  ▼
MySQL
  │
  ▼
paged response + totalMatches
```

This must remain server-side because the requirement explicitly prohibits loading all courses into the browser for filtering/search/sorting/pagination.

---

# 19. API Scope Boundary

The API intentionally does not include endpoints for optional features:

- quizzes
- certificates
- discussion threads
- prerequisites
- video watch tracking
- ratings/reviews
- learning paths
- downloadable resources
- email digests

These are outside the mandatory product boundary.

## 20. Implementation Decisions Still Required

Before implementation, the following technical details should be explicitly decided and recorded:

1. Exact authentication/token configuration.
2. Exact archived-course restore target.
3. Exact pagination defaults and maximum page size.
4. Exact CSV column layout.
5. Exact timestamp/time-zone strategy.
6. Exact inactivity evaluation schedule.
7. Exact HTTP status chosen for state conflicts.
8. Exact request format for uploaded bulk enrollment files.
9. Exact MySQL search strategy for title/description.
10. Exact API versioning policy, if any.

None of these should be treated as additional product requirements unless subsequently approved.
