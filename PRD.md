# Product Requirements Document (PRD)
## Assignment 05 — Course Delivery & Enrollment

## 1. Product Summary

Build a web application that replaces informal internal course distribution and spreadsheet-based completion tracking with a system for course creation, enrollment, lesson delivery, progress tracking, instructor visibility, immutable course history, and inactivity alerts.

The product has two required roles: **Instructor** and **Learner**.

The product must satisfy all ten mandatory assignment goals. Stretch features are optional and must not displace mandatory functionality.

## 2. Product Goals

- Make course creation and publication controlled and reliable.
- Let learners discover and take published courses at their own pace.
- Track learner progress automatically and separately per learner/course.
- Give instructors operational visibility into enrollments, progress, completions, and inactivity.
- Preserve a non-editable record of course changes and comments.
- Replace manual enrollment and spreadsheet progress tracking with system-supported workflows and CSV export.

## 3. Roles and Permissions

| Capability | Instructor | Learner |
|---|---:|---:|
| Sign in with email/password | Yes | Yes |
| View published courses | Yes | Yes |
| View draft courses | Yes | No |
| View archived courses | Yes | No |
| Create course | Yes | No |
| Edit course | Yes | No |
| Publish course | Yes | No |
| Archive course | Yes | No |
| Restore course | Yes | No |
| Add/edit/reorder/remove lessons | Yes | No |
| Enroll self | Not required | Yes |
| Enroll another learner | Yes | No |
| Bulk enroll learners | Yes | No |
| View own enrolled-course list | Yes, where applicable | Yes |
| View own progress | Yes, where applicable | Yes |
| View other learners' progress | Yes | No |
| Search/filter/sort/paginate accessible courses | Yes | Yes |
| View course activity log | Yes | Yes, for courses they can access |
| Add course activity comment | Yes | Yes |
| Edit/delete activity log | No | No |
| Export course learner progress as CSV | Yes | No |
| View instructor inactivity alerts | Yes | No |
| Dismiss inactivity alert | Yes | No |

Server-side authorization is mandatory. UI-only permission hiding is insufficient.

## 4. Data and Domain Requirements

### 4.1 User/account

A user has an email/password account and one of the required roles.

The product must be able to identify learners by email for enrollment and bulk enrollment.

### 4.2 Course

A course has:

- Title.
- Description.
- Category.
- Instructor.
- Lifecycle status.

Required lifecycle:

`Draft → Published → Archived`

Courses can be archived and restored. Archive must preserve lessons and enrollment history.

### 4.3 Lesson

Every lesson belongs to exactly one course and contains:

- Title.
- Content or description.
- Position in course order.

The course lesson view must present lessons in running order.

### 4.4 Enrollment

Enrollment represents a learner enrolled in a course.

The relationship supports:

- One learner → many courses.
- One course → many learners.

Each learner/course combination has its own progress state.

### 4.5 Progress

Required progress states:

`Not Started → In Progress → Completed`

The server rejects any transition outside the required sequence.

### 4.6 Activity history

Every course has an append-only activity log.

Required logged events:

- Course creation.
- Every course edit.
- Every publish transition.
- Every archive transition.
- Comments from learners or the instructor.
- Actor responsible for the event.
- Event timestamp.

No activity record can be edited or deleted after creation.

### 4.7 Inactivity alert state

An inactivity alert concerns a specific learner/course combination.

The alert is triggered when progress is `In Progress` and no further progress has occurred for more than 14 days.

Dismissal applies to that specific learner/course alert. After the learner engages again, a later period of more than 14 days without further progress must produce the alert again.

## 5. Functional Requirements

### FR-01 — Authentication

The system shall provide email/password sign-in for users.

### FR-02 — Server-side role enforcement

The server shall enforce instructor and learner permissions for protected operations.

A learner request attempting to modify course content, enroll another learner, or access another learner's progress shall be rejected by the server.

### FR-03 — Create course

An instructor shall be able to create a course with a title, description, and category.

A newly created course shall be in Draft state.

### FR-04 — Edit course

An instructor shall be able to edit course information after creation.

Every edit must create an activity-log record.

### FR-05 — Publish course

An instructor shall be able to publish a course only when it contains at least one lesson.

If the course is empty, the server must reject the operation and return a message explaining that at least one lesson is required.

### FR-06 — Archive course

An instructor shall be able to archive a course.

Archiving shall remove the course from the learner catalogue while retaining its lessons and enrollment history.

The archive transition must be recorded in the activity log.

### FR-07 — Restore course

An instructor shall be able to restore an archived course.

Restoration shall not remove or rewrite historical lessons or enrollment records.

### FR-08 — Lesson management

An instructor shall be able to:

- Add lessons.
- Edit lessons.
- Reorder lessons.
- Remove lessons.

Each lesson must belong to exactly one course and have a title, content/description, and course position.

### FR-09 — Ordered lesson display

Opening a course shall show its lessons in their defined running order.

### FR-10 — Progress tracking

For each enrolled learner/course pair, the system shall track progress independently.

Allowed sequence:

`Not Started → In Progress → Completed`

The server shall reject invalid transitions.

### FR-11 — Self-enrollment

A learner shall be able to enroll themselves in a published course.

### FR-12 — Instructor enrollment

An instructor shall be able to enroll a learner into a published course.

### FR-13 — Enrolled-course list

A learner shall have one list of every course in which they are enrolled, with their progress in each course.

### FR-14 — Course catalogue

The course list shall show every course accessible to the current viewer:

- Learner: published courses.
- Instructor: drafts, published courses, and archived courses.

### FR-15 — Server-side text search

The course list shall support text search across:

- Course title.
- Course description.

Search must execute on the server.

### FR-16 — Course filters

The course list shall support filters for:

- Category.
- Status.
- Instructor.

Filters must execute on the server.

### FR-17 — Course sorting

The course list shall support sorting by:

- Title.
- Creation date.
- Enrollment count.

Sorting must execute on the server.

### FR-18 — Pagination and match count

The course list shall be paginated and shall show the total number of matches for the current query/filter criteria.

The browser shall not load all courses and filter them locally.

### FR-19 — Bulk enrollment input

An instructor shall be able to provide a list of learner email addresses by:

- Pasting the list, or
- Uploading the list.

### FR-20 — Bulk enrollment result

The bulk enrollment operation shall return a per-address result indicating exactly one of:

- Unknown address.
- Already enrolled learner.
- Newly enrolled.

A failure for one address shall not prevent the result from identifying outcomes for the other submitted addresses.

### FR-21 — CSV progress export

An instructor shall be able to export the progress of every learner enrolled in a course as a CSV file.

### FR-22 — Dashboard headline metrics

The landing view shall show:

- Total learners.
- Published courses.
- Completions this month.
- Learners currently in progress.

### FR-23 — Dashboard enrollment breakdowns

The dashboard shall show enrollment breakdowns:

- By course.
- By progress state.

### FR-24 — Eight-week completion chart

The dashboard shall show completions over the last eight weeks.

### FR-25 — Course activity log

The system shall record course creation, every edit, publish/archive transitions, and learner/instructor comments, together with who performed the event.

### FR-26 — Immutable history

Once an activity-log entry exists, no user, including an instructor, may edit or delete it.

### FR-27 — Inactivity alert generation

An instructor alerts area shall include learners whose course progress is `In Progress` and who have made no further progress for more than 14 days.

### FR-28 — Alert navigation badge

The instructor navigation shall display a count badge for inactivity alerts.

### FR-29 — Alert dismissal

An instructor shall be able to dismiss an alert for a specific learner/course combination.

### FR-30 — Alert reappearance

If a dismissed learner/course alert is followed by learner engagement and then another period of more than 14 days without further progress, the alert shall reappear.

## 6. User Stories

### Authentication and roles

- **US-01:** As an instructor, I want to sign in with my email and password so that I can manage training.
- **US-02:** As a learner, I want to sign in with my email and password so that I can access my courses.
- **US-03:** As an organization, I want role permissions enforced on the server so that users cannot bypass restrictions by manipulating the interface or requests.

### Course management

- **US-04:** As an instructor, I want to create a course with a title, description, and category so that I can prepare training.
- **US-05:** As an instructor, I want to edit course details so that course information stays accurate.
- **US-06:** As an instructor, I want to publish a course once it has content so that learners can take it.
- **US-07:** As an instructor, I want empty-course publication to be rejected so that learners never receive a course with no lessons.
- **US-08:** As an instructor, I want to archive and restore courses so that outdated courses can leave the learner catalogue without losing their history.

### Lessons

- **US-09:** As an instructor, I want to add, edit, reorder, and remove lessons so that I can maintain course content.
- **US-10:** As a learner, I want lessons displayed in order so that I can work through a course consistently.

### Enrollment and progress

- **US-11:** As a learner, I want to enroll myself in a published course so that I can start learning.
- **US-12:** As an instructor, I want to enroll learners so that I can assign training.
- **US-13:** As an instructor, I want to bulk-enroll learners by email list so that I can enroll many learners efficiently.
- **US-14:** As an instructor, I want a result for every submitted email so that I know which enrollments succeeded, were duplicates, or were unknown.
- **US-15:** As a learner, I want to see all my enrolled courses and progress so that I know what remains to complete.
- **US-16:** As a learner, I want my progress to move through the defined states so that completion is tracked automatically.

### Course discovery

- **US-17:** As a viewer, I want to search course titles and descriptions so that I can find relevant courses.
- **US-18:** As a viewer, I want category, status, and instructor filters so that I can narrow results.
- **US-19:** As a viewer, I want title, creation-date, and enrollment-count sorting so that I can organize results.
- **US-20:** As a viewer, I want pagination and total result counts so that I can navigate large course lists.

### Reporting and oversight

- **US-21:** As an instructor, I want to export course learner progress to CSV so that I can use the information outside the application.
- **US-22:** As an instructor, I want dashboard headline metrics so that I can understand training activity at a glance.
- **US-23:** As an instructor, I want enrollment and progress breakdowns so that I can compare course and learner states.
- **US-24:** As an instructor, I want an eight-week completion chart so that I can see recent completion trends.
- **US-25:** As an instructor, I want inactivity alerts so that I can identify learners who have stopped progressing.
- **US-26:** As an instructor, I want to dismiss an inactivity alert so that I can acknowledge it.
- **US-27:** As an instructor, I want an alert to reappear after a new engagement followed by another 14-day inactivity period so that renewed inactivity is not permanently hidden.

### History

- **US-28:** As an instructor, I want course creation and edits recorded so that course history is auditable.
- **US-29:** As a learner or instructor, I want my course comments recorded so that the activity history includes relevant comments.
- **US-30:** As an organization, I want the activity history to be immutable so that past records cannot be rewritten.

## 7. Core User Flows

### Flow A — Instructor creates and publishes a course

1. Instructor signs in.
2. Instructor creates a course with title, description, and category.
3. Course starts as Draft.
4. Instructor adds at least one lesson.
5. Instructor publishes the course.
6. Server verifies that at least one lesson exists.
7. If validation passes, status becomes Published.
8. Creation, edits, and publication are recorded in immutable activity history.
9. The published course becomes available to learners.

**Failure path:** If the course has zero lessons, publication is rejected by the server with an explanatory message and the course remains Draft.

### Flow B — Learner enrolls and progresses

1. Learner signs in.
2. Learner views published courses.
3. Learner enrolls in a published course.
4. The learner/course progress begins as Not Started.
5. As the learner works through lessons, progress advances to In Progress.
6. After completing the required course progression, progress advances to Completed.
7. Invalid state transitions are rejected by the server.
8. The learner can see their own enrolled courses and progress.

### Flow C — Instructor enrolls learners

1. Instructor signs in.
2. Instructor selects a published course.
3. Instructor selects a learner and enrolls them.
4. The learner becomes enrolled in the course.
5. The learner/course progress is tracked independently.

### Flow D — Bulk enrollment

1. Instructor selects a published course.
2. Instructor pastes or uploads a list of email addresses.
3. Server evaluates each address.
4. For every address, the result identifies:
   - Unknown address, or
   - Already-enrolled learner, or
   - Newly enrolled.
5. Instructor receives the per-address results.

### Flow E — Find courses

1. Viewer opens the course list.
2. Viewer enters optional title/description text search.
3. Viewer optionally filters by category, status, and/or instructor.
4. Viewer selects title, creation date, or enrollment count sorting.
5. Server returns the matching page and total number of matches.
6. Viewer navigates between pages.

### Flow F — Export progress

1. Instructor opens a course.
2. Instructor requests learner progress export.
3. Server generates a CSV containing progress for every enrolled learner.
4. CSV is returned for the instructor to use.

### Flow G — Inactivity alert lifecycle

1. Learner/course is In Progress.
2. More than 14 days pass without further progress.
3. The learner/course appears in the instructor alerts area.
4. The instructor navigation shows the alert count.
5. Instructor may dismiss the specific alert.
6. If the learner later engages, the prior inactive condition is no longer the current inactivity episode.
7. If the learner subsequently makes no further progress for more than 14 days, a new alert appears.

## 8. Validation and Error Handling

The following validations are mandatory because the assignment explicitly specifies them:

| Validation | Required behavior |
|---|---|
| Publish empty course | Server rejects; explain that at least one lesson is required. |
| Invalid course state transition | Server rejects. |
| Invalid progress state transition | Server rejects. |
| Learner edits course content | Server rejects. |
| Learner enrolls another learner | Server rejects. |
| Learner accesses another learner's progress | Server rejects. |
| Bulk email unknown | Report `Unknown address` for that address. |
| Bulk email already enrolled | Report `Already-enrolled learner`. |
| Bulk email new learner | Report `Newly enrolled`. |
| Activity history modification | Must not be permitted. |
| Archived course learner catalogue visibility | Archived course must not appear in the learner catalogue. |

Other input-level validation may be implemented as needed by the chosen technology, but this document does not prescribe validation rules that are not stated in the assignment.

## 9. Edge Cases

### Course lifecycle

- Attempting to publish a course with zero lessons must fail.
- A course must retain lessons and enrollment history after archiving.
- Archived courses must not appear in the learner catalogue.
- Lifecycle transitions outside the specified course sequence must not be accepted.

### Lessons

- A lesson cannot belong to multiple courses.
- Lesson order must remain unambiguous after additions, edits, reordering, and removal.
- Removing lessons must not erase unrelated enrollment history.

### Enrollment

- The same learner may be enrolled in many courses.
- A course may contain many learners.
- Re-enrolling an already-enrolled learner must be reported as `Already-enrolled learner` in bulk enrollment.
- An unknown email must be reported rather than silently enrolled.

### Permissions

- Learner permissions cannot be bypassed by calling server endpoints directly.
- Learners must not receive another learner's progress data.
- Learners must not be able to modify course content or enroll other learners.

### Progress

- Progress must be isolated per learner/course pair.
- Invalid state changes must be rejected rather than silently corrected.

### Search

- Search, filtering, sorting, and pagination must remain server-side.
- Total match count must correspond to the current search/filter criteria.

### History

- An activity entry, once recorded, cannot be changed or deleted.
- Course edits and publish/archive transitions must not bypass logging.

### Inactivity

- The threshold is **more than 14 days**, not merely 14 days.
- Dismissing an alert must not permanently suppress future inactivity after a later learner engagement.
- Reappearance applies to the same learner/course relationship after renewed engagement and renewed inactivity.

## 10. Acceptance Criteria

### AC-01 — Accounts and roles

**Given** a user has an instructor or learner role  
**When** they authenticate  
**Then** the application grants only the permissions for that role, with enforcement on the server.

### AC-02 — Course management

**Given** an instructor creates a course  
**When** they provide title, description, and category  
**Then** the course can be saved and managed as a Draft.

**Given** an instructor archives a course  
**When** the archive succeeds  
**Then** the course is absent from the learner catalogue and its lessons/enrollment history remain.

### AC-03 — Publishing

**Given** a course has no lessons  
**When** an instructor attempts to publish it  
**Then** the server rejects the request and explains that at least one lesson is required.

**Given** a course has at least one lesson  
**When** an instructor publishes it  
**Then** its state becomes Published and the transition is logged.

### AC-04 — Lessons

**Given** an instructor manages a course  
**When** they add, edit, reorder, or remove lessons  
**Then** the course reflects the requested lesson structure and opening the course shows lessons in order.

### AC-05 — Progress

**Given** a learner is enrolled in a course  
**When** they progress through the course  
**Then** their progress follows Not Started → In Progress → Completed.

**Given** an invalid transition is requested  
**Then** the server rejects it.

### AC-06 — Enrollment

**Given** a published course  
**When** a learner self-enrolls  
**Then** they become enrolled and can see the course in their enrolled-course list.

**Given** a published course  
**When** an instructor enrolls a learner  
**Then** that learner becomes enrolled.

### AC-07 — Course finding

**Given** a viewer searches, filters, sorts, or paginates the course list  
**When** the request is processed  
**Then** the server performs the operation and returns the current page plus total matches.

### AC-08 — Bulk enrollment

**Given** an instructor supplies multiple email addresses  
**When** bulk enrollment runs  
**Then** each address is independently reported as unknown, already enrolled, or newly enrolled.

### AC-09 — CSV export

**Given** an instructor requests progress for a course  
**When** export is generated  
**Then** the CSV represents the progress of every learner enrolled in that course.

### AC-10 — Dashboard

**Given** an instructor opens the landing/dashboard view  
**Then** it displays total learners, published courses, completions this month, learners currently in progress, enrollment breakdowns by course and progress state, and completions over the last eight weeks.

### AC-11 — Immutable history

**Given** course activity occurs  
**Then** the relevant activity is recorded with the actor and timestamp.

**Given** an existing activity record  
**When** any user attempts to edit or delete it  
**Then** the operation is rejected/not available.

### AC-12 — Inactivity alerts

**Given** a learner is In Progress on a course  
**And** they have made no further progress for more than 14 days  
**Then** the learner/course appears in the instructor alerts area and the navigation badge reflects the alert count.

**Given** an instructor dismisses the alert  
**When** the learner later engages and then becomes inactive for more than 14 days again  
**Then** the alert reappears.

## 11. Non-Functional / Delivery Requirements Explicitly Stated by the Assignment

The assignment does not prescribe a particular application stack. The implementation may use any language, framework, UI library, ORM, and database access approach.

The delivered application must:

- Be working and reachable through a live URL.
- Use free-tier hosting.
- Include enough seeded demo data to demonstrate the system.
- Keep connection strings, keys, and passwords in environment variables rather than the repository.
- Have demo credentials for every role recorded in `SUBMISSION.md`.
- Be published in a public GitHub repository.
- Use incremental git commits after meaningful work.
- Include the five required `docs/` files:
  - `docs/architecture.md`
  - `docs/schema.md`
  - `docs/plan.md`
  - `docs/decisions.md`
  - `docs/ai-prompts.md`
- Complete `SUBMISSION.md`.
- If AI is used, document the actual prompts used, including at least one prompt that produced something wrong and what was corrected. If no AI is used, document that and the process instead.
- Document hosting limitations such as sleeping free-tier services in `SUBMISSION.md` when applicable.

## 12. Explicitly Optional Features

Only after all ten mandatory goals are solidly implemented may the following be considered:

- Quizzes with automatic scoring.
- Certificates.
- Discussion threads per lesson.
- Prerequisite courses.
- Video watch-progress tracking.
- Course ratings/reviews.
- Learning paths.
- Downloadable lesson resources.
- Email digest of inactive learners.

These are not acceptance criteria for the required product.

## 13. Product Boundary

The ten mandatory goals in the assignment are the product cutoff. The implementation should prioritize completeness and correctness of those goals over optional features.
