# Business Requirements Document (BRD)
## Assignment 05 — Course Delivery & Enrollment

## 1. Purpose

This document defines the business requirements for replacing a company's informal course-delivery and spreadsheet-based completion tracking process with a single course delivery and enrollment system.

The system must allow instructors to build and manage courses, learners to take published courses at their own pace, and instructors to understand enrollment and progress without relying on self-reported spreadsheets.

## 2. Business Problem

The current training process uses folders of slide decks and PDFs distributed by email, with completion tracked inconsistently in a spreadsheet.

This creates several business problems:

- A course can be distributed before anyone notices that it contains little or no actual learning content.
- Learners can start a course and stop partway through without the organization noticing.
- Completion information is not reliably maintained.
- It is difficult to distinguish learners who actually completed mandatory training from those who merely opened it.
- Instructors lack a quick view of who has completed training, who is in progress, and who has gone inactive.
- Course history and changes need to be preserved rather than rewritten.

## 3. Business Objectives

The system must:

1. Provide authenticated accounts with instructor and learner roles.
2. Give instructors the ability to create, edit, publish, archive, restore, and manage courses and lessons.
3. Let learners enroll themselves in published courses and work through them at their own pace.
4. Let instructors enroll learners, including through bulk enrollment.
5. Track progress separately for every learner on every course.
6. Prevent invalid course and progress state transitions on the server.
7. Provide server-side course discovery with search, filtering, sorting, pagination, and total match counts.
8. Give instructors visibility into enrollment, completion, in-progress learners, and inactivity.
9. Preserve an immutable activity history for courses.
10. Provide CSV export of course enrollment progress.
11. Provide a dashboard with the required headline metrics and completion trends.

## 4. Stakeholders

| Stakeholder | Business interest |
|---|---|
| Instructors | Build and manage training, enroll learners, monitor progress, review inactivity, and understand course performance. |
| Learners | Discover/enroll in published courses and complete them while tracking their own progress. |
| Organization / training operation | Replace manual course distribution and spreadsheet tracking with reliable, automatically tracked delivery and completion information. |

## 5. Users and Roles

### Instructor

Instructors can:

- Create, edit, publish, archive, and restore courses.
- Manage lessons within courses.
- Enroll learners into published courses.
- Bulk-enroll learners using email addresses.
- View the course catalogue including drafts and archives.
- Search, filter, sort, and paginate courses using the required criteria.
- View enrollment and progress information needed for the dashboard and inactivity alerts.
- Dismiss inactivity alerts.
- Export the progress of every learner enrolled in a course as CSV.
- See course activity history.

### Learner

Learners can:

- Sign in with email and password.
- Enroll themselves in published courses.
- View all courses in which they are enrolled.
- View their progress for each enrolled course.
- Work through lessons and progress through the required progress states.
- Leave comments in the course activity log.

Learners cannot:

- Edit course content.
- Enroll other learners.
- See other learners' progress.

## 6. Scope

### In scope

- Email/password authentication.
- Instructor and learner roles.
- Course creation, editing, publishing, archiving, and restoration.
- Course title, description, and category.
- Lesson creation, editing, reordering, removal, and ordered display.
- Course and learner progress state management.
- Instructor and self-service enrollment.
- Server-side course search, filtering, sorting, and pagination.
- Bulk enrollment with per-address result reporting.
- CSV progress export.
- Instructor dashboard and required metrics/charts.
- Immutable course activity history.
- Instructor inactivity alerts with dismissal/reappearance behavior.
- Deployment as a reachable live application using free tiers.
- Seeded demo data.
- Demo credentials for every role.
- Secure handling of connection strings, keys, and passwords through environment variables.
- Public GitHub repository and incremental git history.
- Required project documentation and submission documentation.

### Out of scope / optional

The following are explicitly optional stretch ideas and are not required for a complete implementation:

- Quizzes with automatic scoring.
- Certificates on completion.
- Discussion threads per lesson.
- Prerequisite courses.
- Video lessons with watch-progress tracking.
- Course ratings and reviews.
- Learning paths.
- Downloadable lesson resources.
- Email digest of inactive learners.

No optional stretch feature substitutes for any of the ten required goals.

## 7. Business Rules

### BR-01: Authentication and authorization

Users sign in with an email and password. At minimum, the system supports instructor and learner roles.

Role restrictions must be enforced on the server, not merely by hiding interface elements.

### BR-02: Course lifecycle

A course follows this lifecycle:

`Draft → Published → Archived`

A course can also be restored from Archived to the appropriate non-archived state as supported by the course lifecycle implementation; restoring must not delete lessons or enrollment history.

Publishing an empty course is prohibited. The server must reject publication when the course has no lessons and explain why.

### BR-03: Course retention

Archiving removes a course from the learner catalogue without deleting its lessons or learner enrollment history.

### BR-04: Lesson ownership and ordering

Every lesson belongs to exactly one course.

Each lesson has:

- Title.
- Content or description.
- Position in the course's running order.

Lessons must be displayed in course order.

### BR-05: Learner progress

For each learner/course combination, progress follows:

`Not Started → In Progress → Completed`

Invalid transitions are rejected by the server.

### BR-06: Enrollment

An instructor can enroll learners into a published course.

A learner can enroll themselves in a published course.

A learner can be enrolled in any number of courses, and a course can have any number of learners.

Each learner must have one view of all courses they are enrolled in, with progress shown for each.

### BR-07: Course visibility

Learners can see published courses.

Instructors can see all courses, including drafts and archived courses.

### BR-08: Course finding

Course search and discovery must be performed on the server.

The searchable/filterable/sortable course list supports:

- Text search over title and description.
- Category filter.
- Status filter.
- Instructor filter.
- Sorting by title, creation date, or enrollment count.
- Pagination.
- Total number of matching results.

The browser must not load every course and perform these operations locally.

### BR-09: Bulk enrollment results

For each pasted/uploaded email address, bulk enrollment must report one of these outcomes:

- Unknown address.
- Already-enrolled learner.
- Newly enrolled.

### BR-10: Progress export

The system must provide a CSV export containing the progress of every learner enrolled in a course.

### BR-11: Dashboard

The landing/dashboard view must include:

- Total learners.
- Published courses.
- Completions this month.
- Learners currently in progress.
- Enrollment breakdown by course.
- Enrollment breakdown by progress state.
- Completion chart covering the last eight weeks.

### BR-12: Immutable history

Every course has an activity log recording:

- Creation.
- Every edit.
- Every publish transition.
- Every archive transition.
- Who performed the action.
- Comments left by learners or the instructor.

The activity log cannot be edited or deleted after the fact, including by an instructor.

### BR-13: Inactivity alerts

A learner appears in an instructor's alerts when:

- Their course progress is `In Progress`, and
- They have made no further progress for more than 14 days.

The instructor navigation displays an alert count badge.

An instructor can dismiss an alert for a specific learner/course combination.

If that learner engages again and subsequently becomes inactive for the same period, the alert must reappear.

## 8. Success Criteria

The solution is business-successful when all ten required goals are implemented and demonstrable:

1. Role-based authenticated access works and restrictions are enforced server-side.
2. Instructors can manage courses through the required lifecycle.
3. Lessons can be managed and displayed in order.
4. Required course and progress state transitions are enforced.
5. Enrollment works for both instructors and learners with per-learner progress.
6. Course discovery supports all required server-side search/filter/sort/pagination behavior and total counts.
7. Bulk enrollment reports the three required outcomes per address and progress can be exported to CSV.
8. The dashboard contains all required metrics, breakdowns, and eight-week completion chart.
9. Course history is complete and immutable.
10. Inactivity alerts follow the exact 14-day, dismissal, and reappearance rules.

## 9. Assignment Delivery Success Criteria

The assignment also requires the submission to be:

- Published in a public GitHub repository.
- Built with incremental commits after meaningful work rather than one final commit.
- Deployed at a reachable live URL using free tiers.
- Seeded with enough demo data to demonstrate the system.
- Provided with demo credentials for every role in `SUBMISSION.md`.
- Configured so connection strings, keys, and passwords are kept in environment variables and not committed.
- Accompanied by completed `SUBMISSION.md`.
- Accompanied by the five required documents under `docs/`: architecture, schema, plan, decisions, and AI prompts.
- Documented with actual AI prompts used, including a bad-output example and correction if AI was used.
- Delivered within the assignment's approximately 12-hour size guideline; this is a size guide rather than a race or a scoring requirement for finishing early.

## 10. Constraints

- Any language, framework, UI library, ORM, and database approach may be used.
- No particular technology stack receives a preference according to the assignment.
- Free-tier hosting is required for the deployed solution.
- The ten stated goals are the required scope cutoff.
