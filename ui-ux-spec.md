# UI/UX Specification
## Assignment 05 — Course Delivery & Enrollment

> This specification defines the user interface and user experience for the mandatory product scope described in the PRD. It is an implementation-ready UI/UX contract, not application code.

---

## 1. Purpose

The application replaces email-distributed course folders/PDFs and spreadsheet-based completion tracking with a single interface for:

- Instructor course and lesson management.
- Learner course discovery and learning.
- Instructor and learner enrollment workflows.
- Per-learner/course progress tracking.
- Course search, filtering, sorting, and pagination.
- Bulk enrollment and CSV progress export.
- Instructor dashboard and inactivity alerts.
- Immutable course activity history and comments.

The UI must reflect the PRD while keeping authorization and business rules on the server.

---

## 2. UX Principles

### 2.1 Role clarity

The interface must make the current user's role obvious and expose only actions relevant to that role.

Hiding an action is a UX behavior only. It is never the authorization mechanism; protected requests must still be rejected by the server.

### 2.2 State visibility

Course lifecycle and learner progress are important business states. Their current state should be visible wherever it affects the available actions.

Use consistent status badges for:

- Course: `Draft`, `Published`, `Archived`.
- Progress: `Not Started`, `In Progress`, `Completed`.

Exact colors are an implementation choice, but status indicators must remain distinguishable without relying on color alone.

### 2.3 Server is authoritative

The UI must treat server responses as authoritative for:

- Permissions.
- Course lifecycle transitions.
- Progress transitions.
- Enrollment results.
- Search/filter/sort/pagination.
- Dashboard metrics.
- Inactivity alert state.

The UI must not assume that a hidden/disabled control guarantees authorization.

### 2.4 Clear feedback

Every mutation should provide a clear outcome:

- Loading/in-progress indication.
- Success confirmation where useful.
- Actionable validation/error feedback.
- No silent failures.

### 2.5 Preserve history

Activity history is immutable. The UI must never present edit/delete controls for existing activity records.

### 2.6 Scope discipline

Do not add UI for optional stretch features such as quizzes, certificates, lesson discussion threads, prerequisites, video watch tracking, ratings/reviews, learning paths, downloadable resources, or email digests.

A comment is a course activity-log entry, not a separate discussion feature.

---

# 3. Application Shell

## 3.1 Shared authenticated shell

After sign-in, the application uses:

1. Persistent navigation/sidebar or responsive navigation.
2. Application/page title.
3. Main content area.
4. Contextual actions for the current screen.
5. User/role indicator.
6. Global feedback area for transient success/error messages.

### Desktop navigation — Instructor

Recommended order:

- Dashboard
- Courses
- My Courses
- Alerts
- User/account area

Course-specific screens can be reached from Courses and course detail/manage views.

### Desktop navigation — Learner

Recommended order:

- Course Catalogue
- My Courses
- User/account area

Learners do not receive instructor navigation for:

- Dashboard.
- Alerts.
- Course management.
- Enrollment management.
- CSV export.

### 3.2 Navigation behavior

- The active section is visually indicated.
- Navigation must never expose another user's protected data.
- Selecting a course from a list opens the appropriate course view for the role.
- Back navigation should preserve the previous list's search/filter/sort/page state where practical.
- A browser refresh must not change the server-authorized role or course visibility.

### 3.3 Global states

**Initial application loading**
- Show a neutral application-level loading state.
- Do not render protected content until authentication/authorization state is known.

**Authenticated**
- Render role-specific shell.

**Unauthenticated**
- Redirect to Login.

**Unauthorized/Forbidden**
- Show a clear permission message and provide navigation back to an accessible area.
- Do not expose protected response data.

**Unexpected server error**
- Show a concise error message.
- Provide a Retry action where retrying is meaningful.

---

# 4. Screen Inventory

| ID | Screen | Instructor | Learner |
|---|---|---:|---:|
| AUTH-01 | Login | Yes | Yes |
| INS-01 | Instructor Dashboard | Yes | No |
| COURSE-01 | Course Catalogue | Yes | Yes |
| COURSE-02 | My Courses | Yes, where applicable | Yes |
| COURSE-03 | Course View / Learning | Yes, where applicable | Yes |
| INS-02 | Create Course | Yes | No |
| INS-03 | Edit Course | Yes | No |
| INS-04 | Course Management | Yes | No |
| INS-05 | Lesson Editor | Yes | No |
| INS-06 | Enrollment Management | Yes | No |
| INS-07 | Bulk Enrollment | Yes | No |
| INS-08 | Learner Progress | Yes | No |
| INS-09 | Activity History | Yes | Yes |
| INS-10 | Inactivity Alerts | Yes | No |
| COMMON-01 | Not Found / Access Error | Yes | Yes |

---

# 5. AUTH-01 — Login

## Purpose

Authenticate instructors and learners using email/password.

## Layout

- Application/product identity.
- Page heading: `Sign in`.
- Email field.
- Password field.
- Sign-in button.
- Inline authentication error area.

Do not add registration, password reset, social login, or other authentication flows unless separately required by the implementation.

## Form

| Field | Required | UI behavior |
|---|---|---|
| Email | Yes | Email input; browser-level format validation may be used. |
| Password | Yes | Password input; provide show/hide affordance only if desired. |

## Validation

- Empty required fields: show field-level validation.
- Invalid credentials: show a non-sensitive authentication error.
- Server/authentication failure: show retryable error.
- Do not reveal whether a particular account exists beyond the server's authentication contract.

## Loading

After submitting:

- Disable the submit action.
- Show progress indication.
- Prevent accidental duplicate submission.

## Success

Redirect to the role-appropriate landing view:

- Instructor → Dashboard.
- Learner → Course Catalogue or My Courses according to the implementation's landing choice.

The PRD explicitly requires the instructor landing/dashboard view; it does not prescribe a learner landing page.

---

# 6. INS-01 — Instructor Dashboard

## Purpose

Give instructors an operational overview of training activity.

## Required content

### Headline metric cards

1. **Total Learners**
2. **Published Courses**
3. **Completions This Month**
4. **Learners In Progress**

Each card contains:

- Metric label.
- Current value.
- Loading/skeleton state.
- Error state if its data cannot be loaded.

Do not invent additional KPI requirements.

### Enrollment breakdown by course

Use a table or chart that makes course-level enrollment counts understandable.

Minimum useful columns:

| Course | Enrollment count |
|---|---:|

If progress-state breakdown is presented alongside it, keep the dimensions clear.

### Enrollment breakdown by progress state

Show counts for:

- Not Started.
- In Progress.
- Completed.

A compact chart or table is acceptable.

### Eight-week completion chart

Show completions over the last eight weeks.

Requirements:

- Eight distinct weekly periods.
- Clear week labels.
- Completion count for each period.
- Empty weeks should display as zero rather than disappearing.
- Tooltip/details may be used as an implementation choice.

The UI must not imply that the chart is a prediction; it is a historical completion view.

## Loading state

Use:

- Four metric-card skeletons.
- Skeleton for the course breakdown.
- Skeleton for progress breakdown.
- Chart skeleton.

Avoid showing stale or partial metrics as if they were current.

## Empty state

If there is no underlying training data:

> No training activity yet.

Explain only what is supported by the product, for example that dashboard data will appear after courses/enrollments/progress exist.

## Error state

If a dashboard request fails:

- Show the affected section's error.
- Provide `Retry`.
- Keep successfully loaded independent sections visible where the API architecture permits independent loading.

Do not replace all dashboard data with a generic blank screen when only one widget failed.

---

# 7. COURSE-01 — Course Catalogue

## Purpose

Provide one server-backed list of all courses the current role can see.

## Role-specific visibility

### Instructor

Can see:

- Draft courses.
- Published courses.
- Archived courses.

### Learner

Can see:

- Published courses only.

Archived courses must not appear in the learner catalogue.

## Page structure

1. Page title.
2. Search field.
3. Filters.
4. Sort control.
5. Result count.
6. Course table/grid.
7. Pagination.
8. Relevant course actions.

## Search

One search field covering:

- Course title.
- Course description.

Recommended placeholder:

`Search courses by title or description`

Behavior:

- Search is submitted to the server.
- Debounce may be used as an implementation choice.
- Clear search restores the unsearched result set.
- Current query remains visible while navigating pages.

## Filters

### Category

- Select/dropdown.
- Supports the categories returned by the application.
- Clearable.

### Status

Required for the course list.

- Draft.
- Published.
- Archived.

For learners, the UI should not offer a status choice that could imply access to archived/draft courses; their accessible result set remains published courses.

### Instructor

- Select/dropdown.
- Instructor filter applies to the server-side course query.
- Learners may only see instructors associated with accessible published courses.

## Sorting

Required options:

- Title.
- Creation date.
- Enrollment count.

The UI should make ascending/descending direction explicit or obvious.

## Result count

Display:

`Showing [current range] of [total matches]`

or an equivalent clear representation.

The total must come from the server's current search/filter criteria.

## Course list columns/cards

Recommended table columns:

| Column | Instructor | Learner |
|---|---:|---:|
| Title | Yes | Yes |
| Description/category | Yes | Yes |
| Instructor | Yes | Yes |
| Status | Yes | Optional but useful |
| Enrollment count | Yes | Not required |
| Progress | Where applicable | Yes, where enrolled |
| Action | Yes | Yes |

Do not expose instructor-only learner progress to learners.

## Row actions

### Instructor

Depending on status and backend authorization:

- Open/manage.
- Edit.
- Publish.
- Archive.
- Restore.

Only show actions meaningful for the current state, while still handling server rejection.

### Learner

- Open course.
- Enroll if not already enrolled.

For an already enrolled course, show the enrolled/progress state instead of offering a duplicate enrollment action.

## Loading

- Initial page: table/card skeleton.
- Search/filter/sort/page change: retain layout and show a localized loading state.
- Disable conflicting repeated actions during request execution.

## Empty states

### No courses at all

`No courses available.`

### Search/filter produced no results

`No courses match your search or filters.`

Provide:

`Clear filters` / `Clear search`

when applicable.

### Instructor with no courses

Provide `Create course` as the primary next action.

Do not show this action to learners.

## Errors

- Course list request failure → retry.
- Invalid server query → show server error without losing the rest of the page.
- Forbidden → access error and redirect/navigation.
- Unexpected error → generic retryable error.

---

# 8. COURSE-02 — My Courses

## Purpose

Show courses in which the current learner is enrolled, with their progress.

## Learner view

Required:

- Every enrolled course.
- Progress state for each course.

Recommended card/table columns:

| Course | Category | Instructor | Progress |
|---|---|---|---|

Optional progress presentation can include:

- Status badge.
- Progress indicator if the backend exposes a numeric representation.

Do not invent a percentage if the backend only provides the three required states.

## Instructor view

The PRD says instructors can have an own enrolled-course list "where applicable." This screen should therefore be implemented only if the selected architecture/product flow supports instructor enrollment as a learner-like relationship.

The mandatory instructor oversight requirement is the course/learner progress view, not an instructor self-learning workflow.

## Empty state

Learner:

`You are not enrolled in any courses yet.`

Provide a clear link/button to `Browse Courses`.

## Loading

Show course-list skeletons.

## Error

Show retryable error with no fabricated course data.

---

# 9. COURSE-03 — Course View / Learning

## Purpose

Let a learner open an accessible course and work through its lessons in order.

## Header

Show:

- Course title.
- Description.
- Category.
- Current progress state, when the user is enrolled.
- Course status where relevant.

## Lesson navigation

Lessons must be displayed in their defined running order.

Recommended layout:

- Left/side lesson list on larger screens.
- Current lesson content in main area.
- Stacked lesson navigation on smaller screens.

Each lesson entry shows:

- Position.
- Lesson title.
- Current selection.
- Completion/progress indicator only if supported by the backend.

Do not invent lesson-level completion semantics if they are not part of the API/domain model.

## Progress interaction

The UI should expose only progress actions supported by the backend.

Required state sequence:

`Not Started → In Progress → Completed`

The UI must not manufacture an invalid transition.

If the implementation provides an action such as `Start` or `Mark complete`, the server remains authoritative. If the server rejects it, show the returned explanation and refresh the authoritative state.

## Learner restrictions

Learners cannot:

- Edit course title/description/category.
- Add/edit/reorder/remove lessons.
- Publish/archive/restore.
- Enroll another learner.
- View another learner's progress.

No learner UI should expose these controls.

## Instructor view

An instructor may open/manage their accessible courses, but content-editing controls belong to the management screens.

The instructor must not be shown learner-only personal progress for another learner unless the screen is explicitly an instructor progress view.

## Loading

- Course header skeleton.
- Lesson navigation skeleton.
- Lesson content skeleton.

## Empty

If a course has no lessons:

- For a Draft course, instructor sees a management-oriented empty state prompting lesson creation.
- For a Published course, the backend should prevent the normal publish path from creating this condition. If encountered, show a data-integrity/error state rather than inventing content.

## Errors

- Course not found → Not Found.
- Course not accessible → Forbidden/access error.
- Lesson load failure → retry lesson/content.
- Progress mutation failure → show error and refresh current progress.

---

# 10. INS-02 — Create Course

## Purpose

Allow an instructor to create a Draft course.

## Form

| Field | Required | Description |
|---|---|---|
| Title | Yes | Course title. |
| Description | Yes/implementation-defined input requirement | Course description. |
| Category | Yes/implementation-defined input requirement | Course category. |

The PRD requires these course fields but does not define maximum lengths or exact category vocabulary. Those should be implementation-level validation decisions and documented separately.

## Actions

- `Create course`
- `Cancel`

## Submit behavior

1. Validate required fields.
2. Show submitting state.
3. Send to server.
4. On success, course is Draft.
5. Redirect to Course Management or Course Detail with the new course loaded.

Creation must appear in activity history.

## Errors

- Field validation → inline.
- Server validation → field/general error as appropriate.
- Network/server failure → retry while preserving entered data.

---

# 11. INS-03 — Edit Course

## Purpose

Allow an instructor to edit course information after creation.

## Form

Same core fields as Create Course:

- Title.
- Description.
- Category.

Show:

- Current course status.
- Last/currently visible context if provided by API.

## Actions

- `Save changes`
- `Cancel`

## Important UX rule

Every successful edit creates an activity-log entry. The UI should not attempt to update history separately.

## Loading

- Load existing values before editing.
- Disable save during submission.
- Preserve unsaved values if a save request fails.

## Error

If the server rejects the edit:

- Keep the form populated.
- Show the server message.
- Do not claim that the edit succeeded.

---

# 12. INS-04 — Course Management

## Purpose

Provide the instructor's central management view for one course.

## Header

Display:

- Course title.
- Category.
- Description.
- Instructor.
- Lifecycle status badge.

## Primary actions

Actions depend on server-allowed lifecycle state:

- Edit course.
- Manage lessons.
- Publish.
- Archive.
- Restore.
- Manage enrollment.
- View learner progress.
- Export progress CSV.
- View activity history.

Do not hard-code an assumed restore destination in the UI because the PRD does not specify exactly which non-archived state restoration targets.

## Publish interaction

Clicking `Publish` should:

1. Open confirmation or immediate action according to implementation preference.
2. Submit to server.
3. Show loading state.
4. On success, change status to Published and refresh related data.
5. On empty course, show the server's explanatory error that at least one lesson is required.

The UI may proactively warn when there are zero lessons, but must still rely on the server.

## Archive interaction

Show confirmation because archiving changes learner visibility.

Confirmation should clearly state:

- The course will leave the learner catalogue.
- Lessons and enrollment history are retained.

On success:

- Status becomes Archived.
- Refresh course list/management data.
- Activity history reflects the transition.

## Restore interaction

Show `Restore` only when appropriate.

On success:

- Refresh status and available actions.
- Do not assume a particular restored state unless returned by the server.

---

# 13. INS-05 — Lesson Management / Editor

## Purpose

Allow instructors to maintain ordered lessons within a course.

## Lesson management list

Show:

| Column | Purpose |
|---|---|
| Position | Running order. |
| Title | Lesson identity. |
| Content/description preview | Optional concise preview. |
| Actions | Edit/remove. |

Provide:

- `Add lesson`
- Reorder controls.

## Reordering

The UI may use:

- Drag-and-drop.
- Move up/down controls.

The chosen interaction must produce one unambiguous course order.

After reordering:

- Show saving state.
- Refresh positions from server.
- If save fails, restore/display the server-authoritative order.

## Add/Edit lesson form

Fields:

| Field | Required |
|---|---|
| Title | Yes |
| Content or description | Yes |

The PRD requires title and content/description but does not prescribe a rich-text editor, maximum lengths, or formatting rules.

## Remove lesson

Use confirmation.

The confirmation should identify the lesson and make clear that the lesson will be removed from the course structure.

Do not imply that unrelated enrollment history is deleted.

## Empty state

For a course with zero lessons:

`No lessons yet.`

Primary action:

`Add lesson`

Secondary contextual action for an unpublished course:

`Publish` should not be offered as enabled; publication must remain blocked until a lesson exists.

---

# 14. INS-06 — Enrollment Management

## Purpose

Allow an instructor to enroll learners into a published course.

## Preconditions

Enrollment is for published courses.

If the course is not published:

- Do not present the normal enrollment action as available.
- If the server rejects a request, show the server error.

## Single learner enrollment

Provide an instructor-facing learner selector/search.

The exact learner-selection control is an implementation choice; the business requirement is that the instructor can select a learner and enroll them.

Recommended result table:

| Learner | Email | Enrollment status | Action |
|---|---|---|---|
| Learner identity | Email | Not enrolled / Already enrolled | Enroll |

Do not expose information beyond what is necessary for the instructor's enrollment task.

## Duplicate enrollment

If already enrolled:

- Disable or replace the enroll action.
- Display `Already enrolled`.
- If the server reports the duplicate after submission, display that result.

---

# 15. INS-07 — Bulk Enrollment

## Purpose

Enroll multiple learners efficiently using email addresses.

## Input modes

The screen must support:

1. Pasting an email list.
2. Uploading the list.

The exact file format is an implementation choice unless constrained elsewhere; the UI should clearly state what format the selected implementation accepts.

## Layout

1. Course context.
2. Input method tabs/toggle if desired.
3. Email list input/upload area.
4. `Process enrollments` action.
5. Results table.

## Paste mode

Large multiline input:

`Paste learner email addresses, one per line`

Do not require users to manually transform the list into a special format unless the implementation needs it.

## Upload mode

Show:

- File picker/drop area.
- Selected filename.
- Remove/replace file action.
- Accepted format information.

## Processing

During processing:

- Disable submission.
- Show progress/loading indicator.
- Do not clear input until outcome is known.

## Result table

Every submitted address must receive exactly one required business outcome:

| Email | Result |
|---|---|
| learner@example.com | Newly enrolled |
| existing@example.com | Already-enrolled learner |
| unknown@example.com | Unknown address |

The three result labels should be visually distinct.

## Partial success

A failure/outcome for one address must not prevent the UI from displaying outcomes for the other addresses.

## Empty input

Show:

`Enter or upload at least one learner email address.`

## Processing error

If the entire request fails:

- Preserve the submitted input.
- Show retry.
- Do not fabricate per-address results.

---

# 16. INS-08 — Learner Progress

## Purpose

Give instructors visibility into learner progress for a course.

## Table

Recommended columns:

| Column | Description |
|---|---|
| Learner | Learner identity. |
| Email | Learner email. |
| Progress | Not Started / In Progress / Completed. |
| Last progress activity | Show only if supplied by backend; useful for inactivity context. |
| Alert state | Optional indicator when an inactivity alert applies. |

The mandatory business requirement is the learner/course progress view; exact extra columns depend on available API data.

## Permissions

Instructor only.

Learners must never receive this dataset.

## Empty state

`No learners are enrolled in this course.`

## Loading

Use row skeletons.

## Error

Show retryable server error.

## CSV export

Provide `Export CSV`.

On click:

- Show generating/loading state.
- Prevent duplicate export requests.
- On success, return the generated CSV to the user.
- On failure, show a clear retryable error.

---

# 17. INS-09 — Activity History

## Purpose

Show the immutable course activity log.

## Access

- Instructor: for accessible courses.
- Learner: for courses they can access.

## Activity list

Recommended table:

| Timestamp | Actor | Event | Details |
|---|---|---|---|

Events include:

- Course creation.
- Course edit.
- Publish.
- Archive.
- Comment.

The activity record must identify who performed it and when.

## Comments

Provide a comment input for users allowed to comment on the course.

Form:

- Comment text.
- `Add comment`.

The comment becomes a new immutable activity entry.

## History immutability

Existing rows must have:

- No edit button.
- No delete button.
- No inline editing.

Do not expose an administrative override.

## Empty state

`No activity recorded for this course yet.`

## Loading

Chronological list/table skeleton.

## Error

Retryable history-load error.

## Comment submission error

- Preserve typed comment.
- Show server error.
- Do not insert a fake activity row.

## Ordering

The implementation may choose newest-first or oldest-first, but it must be consistent and clearly timestamped.

---

# 18. INS-10 — Inactivity Alerts

## Purpose

Give instructors a focused list of learner/course relationships that have been inactive for more than 14 days while In Progress.

## Navigation badge

The instructor navigation must show an alert count badge.

The count must come from the server's current alert state.

## Alert list

Recommended columns:

| Learner | Course | Progress | Inactivity context | Action |
|---|---|---|---|---|
| Learner | Course | In Progress | Last progress/activity information if supplied | Dismiss |

The UI should not claim that an alert is exactly 14 days old. The trigger is **more than 14 days**.

## Dismiss action

Use a clear action such as:

`Dismiss`

On confirmation or direct action:

- Submit to server.
- Show loading state.
- Remove/update the alert only after successful server response.
- Refresh badge count.

## Reappearance

The UI does not need to manually create a future alert.

It should simply reflect server state:

- Learner engages again.
- A later period of more than 14 days without further progress occurs.
- Server generates/re-exposes the alert.
- Alert count and list reflect it.

## Empty state

`No inactive learners to review.`

This is a positive operational state; do not present it as an error.

## Loading

- Badge may show a temporary loading indicator if necessary.
- Alert list skeleton.
- Dismiss button disabled during request.

## Error

- Alert list failure → retry.
- Dismiss failure → keep the alert visible and show the server error.
- Badge refresh failure → avoid replacing a known count with an invented zero.

---

# 19. COMMON-01 — Not Found / Access Error

## Not Found

Use when the requested resource does not exist.

Message:

`The requested course or page could not be found.`

Provide navigation back to an accessible course/list/dashboard area.

## Forbidden

Use when the server denies access.

Message should make the permission boundary clear without leaking protected resource information.

For example:

`You do not have permission to access this resource.`

Do not expose another learner's progress or private course information in the error.

---

# 20. Tables

## 20.1 Course catalogue table

Required behaviors:

- Server-side search.
- Server-side filters.
- Server-side sorting.
- Server-side pagination.
- Total result count.

Do not implement full-data client filtering as a substitute.

## 20.2 Learner enrolled-course table

Required:

- Every enrolled course.
- Progress state.

## 20.3 Instructor learner-progress table

Required:

- Every learner enrolled in selected course.
- Their progress.

## 20.4 Bulk enrollment result table

Required:

- Every submitted address.
- Exactly one of the three required outcome categories.

## 20.5 Activity history table

Required:

- Timestamp.
- Actor.
- Event/comment information.

Immutable.

## 20.6 Alert table

Required:

- Learner/course alert relationship.
- Dismiss action for instructor.

---

# 21. Forms and Form UX

## Common rules

All forms should:

- Clearly label required fields.
- Validate before submission where possible.
- Still handle server validation.
- Preserve user-entered values after recoverable failure.
- Disable duplicate submission while a request is running.
- Show success only after the server confirms the mutation.

## Form list

| Form | Role | Purpose |
|---|---|---|
| Login | Both | Authenticate. |
| Course create | Instructor | Create Draft. |
| Course edit | Instructor | Update course details. |
| Lesson create | Instructor | Add lesson. |
| Lesson edit | Instructor | Update lesson. |
| Single enrollment | Instructor | Enroll a learner. |
| Bulk enrollment | Instructor | Enroll multiple learners. |
| Activity comment | Both | Add immutable comment event. |
| Progress action | Learner | Request an allowed progress transition, if exposed by implementation. |

---

# 22. Search, Filter, Sort, and Pagination UX

## Search

- Search input remains visible above the list.
- Current search term is retained across pagination.
- Clear action is available when a query exists.

## Filters

Use a consistent filter area.

Recommended:

- Category.
- Status.
- Instructor.

Provide:

- `Apply` if filters are submitted as a batch, or
- Immediate server requests if filters update instantly.

Either pattern is acceptable as long as the resulting query is server-side.

## Sort

Provide one sort field and direction, or a clearly understandable combined sort control.

Required fields:

- Title.
- Creation date.
- Enrollment count.

## Pagination

Display:

- Current page.
- Total pages where calculable.
- Previous/next controls.
- Current result range.
- Total match count.

Disable impossible navigation actions.

## Query state

The UI should preserve search/filter/sort/page state during ordinary navigation where practical.

When filters change, reset to page 1 to avoid requesting an invalid/empty later page for the new criteria.

This is a UI behavior choice, not a business rule.

---

# 23. Dashboard UX Details

## Metric card states

### Loading
Skeleton value and label.

### Loaded
Clear metric and label.

### Zero
Show `0`, not an empty placeholder.

### Error
Show a compact error with retry.

## Breakdown visualization

The course enrollment breakdown should remain understandable with many courses. A table is preferred if a chart would become unreadable.

The progress-state breakdown can use a compact chart or three-value summary.

## Eight-week chart

The x-axis should contain eight weekly periods. The y-axis should represent completion count.

Hover/focus details may expose the exact count.

Accessibility:

- Chart must have an accessible text summary/table equivalent or otherwise expose values to assistive technology.
- Do not rely solely on color.

---

# 24. Loading State Specification

| Context | Loading UI |
|---|---|
| Login submit | Button spinner/disabled state |
| Dashboard | Card/table/chart skeletons |
| Course list | Row/card skeletons |
| Course detail | Header + lesson skeleton |
| Lesson list | Row skeletons |
| Course save | Disabled submit + progress |
| Lesson save | Disabled submit + progress |
| Publish/archive/restore | Action-specific progress |
| Enrollment | Action-specific progress |
| Bulk enrollment | Processing state |
| Progress mutation | Local progress control loading |
| Activity history | List skeleton |
| Comment submit | Disabled submit + progress |
| Alerts | List skeleton |
| Alert dismiss | Local action loading |
| CSV export | Export action loading |

Avoid full-screen loading for small localized mutations unless the operation genuinely blocks the whole page.

---

# 25. Empty State Specification

Every collection screen must distinguish "no data" from "request failed."

## Course catalogue

- No courses: `No courses available.`
- No search/filter matches: `No courses match your search or filters.`

## My Courses

`You are not enrolled in any courses yet.`

Action: `Browse Courses`.

## Lessons

`No lessons yet.`

Action: `Add lesson` for instructors.

## Learner progress

`No learners are enrolled in this course.`

## Activity

`No activity recorded for this course yet.`

## Alerts

`No inactive learners to review.`

## Dashboard

`No training activity yet.`

The exact wording can be polished during implementation, but the distinction between zero/empty and error must remain.

---

# 26. Error State Specification

## Error categories

### 26.1 Validation error

Show next to the relevant field where possible.

### 26.2 Business-rule rejection

Show a clear server-provided explanation.

Mandatory example:

`Cannot publish this course. Add at least one lesson before publishing.`

### 26.3 Authorization error

Show access denied without protected data.

### 26.4 Not found

Show resource-not-found state.

### 26.5 Network/server error

Show:

- What failed.
- Retry action where appropriate.

### 26.6 Partial bulk results

Do not replace successful rows with a generic error. Show every per-address result returned by the server.

---

# 27. Success and Confirmation UX

Use lightweight confirmation for ordinary successful operations.

Examples:

- `Course created.`
- `Course updated.`
- `Course published.`
- `Course archived.`
- `Course restored.`
- `Lesson saved.`
- `Learner enrolled.`
- `Bulk enrollment processed.`
- `Comment added.`
- `Alert dismissed.`

Success messages should appear only after server confirmation.

Destructive or visibility-changing operations such as archive and lesson removal should use a confirmation step.

---

# 28. Role-Specific Behavior Matrix

| Screen/Action | Instructor | Learner |
|---|---|---|
| Login | ✓ | ✓ |
| Dashboard | ✓ | — |
| View published courses | ✓ | ✓ |
| View drafts | ✓ | — |
| View archived courses | ✓ | — |
| Create course | ✓ | — |
| Edit course | ✓ | — |
| Publish | ✓ | — |
| Archive | ✓ | — |
| Restore | ✓ | — |
| Add/edit/reorder/remove lessons | ✓ | — |
| Self-enroll | Not required | ✓ |
| Enroll another learner | ✓ | — |
| Bulk enrollment | ✓ | — |
| View own enrolled courses | Where applicable | ✓ |
| View own progress | Where applicable | ✓ |
| View other learners' progress | ✓ | — |
| Search/filter/sort/paginate accessible courses | ✓ | ✓ |
| Activity history | ✓ | ✓ |
| Add activity comment | ✓ | ✓ |
| Edit/delete history | — | — |
| CSV export | ✓ | — |
| Inactivity alerts | ✓ | — |
| Dismiss alerts | ✓ | — |

The UI must mirror this matrix while the server independently enforces it.

---

# 29. Navigation Flows

## 29.1 Authentication flow

```text
Login
  ↓
Authenticate
  ↓
Role resolved
  ├── Instructor → Dashboard
  └── Learner → Course Catalogue / My Courses
```

## 29.2 Instructor course creation flow

```text
Dashboard
  ↓
Courses
  ↓
Create Course
  ↓
Course Management (Draft)
  ↓
Manage Lessons
  ↓
Add Lesson
  ↓
Course Management
  ↓
Publish
  ├── No lessons → Error; remain Draft
  └── Has lessons → Published
```

## 29.3 Instructor course maintenance flow

```text
Courses
  ↓
Select Course
  ↓
Course Management
  ├── Edit Course
  ├── Manage Lessons
  ├── Enrollment
  ├── Learner Progress
  ├── Activity History
  ├── Export CSV
  └── Lifecycle action
       ├── Publish
       ├── Archive
       └── Restore
```

## 29.4 Learner discovery/enrollment flow

```text
Course Catalogue
  ↓
Search/filter/sort
  ↓
Course
  ↓
Enroll
  ↓
My Courses
  ↓
Open Course
  ↓
Lessons in order
  ↓
Progress transitions
```

## 29.5 Instructor enrollment flow

```text
Courses
  ↓
Published Course
  ↓
Enrollment Management
  ├── Select learner → Enroll
  └── Bulk Enrollment → Paste/upload → Results
```

## 29.6 Progress oversight flow

```text
Course Management
  ↓
Learner Progress
  ↓
View enrolled learners + progress
  ├── Export CSV
  └── Follow relevant inactivity alert
```

## 29.7 Activity flow

```text
Course
  ↓
Activity History
  ├── Read immutable events
  └── Add comment
       ↓
    New immutable activity entry
```

## 29.8 Alert flow

```text
Instructor shell
  ↓
Alert badge
  ↓
Alerts
  ↓
Select learner/course alert
  ├── Open course/progress context
  └── Dismiss
       ↓
    Badge/list refresh
```

---

# 30. Responsive and Accessibility Guidance

The PRD does not prescribe exact breakpoints, colors, typography, or component library. These are implementation decisions.

Recommended baseline:

- Desktop navigation should collapse appropriately on smaller screens.
- Tables should remain usable on narrow screens through responsive columns, horizontal scrolling, or an equivalent presentation.
- Forms should remain usable without horizontal overflow.
- All actionable controls must be keyboard accessible.
- Form fields require visible labels.
- Focus states must be visible.
- Status should not be communicated by color alone.
- Charts should expose accessible values.
- Error messages should be associated with their relevant controls where possible.
- Confirmation dialogs must be keyboard accessible.

These are UX quality requirements rather than additional business functionality.

---

# 31. Data Freshness and Refresh Behavior

Because dashboard, alerts, enrollment, and progress data can change:

- After a successful mutation, refresh the affected server-backed view.
- After dismissing an alert, refresh the badge count.
- After enrollment, refresh enrollment status and relevant learner/course lists.
- After publish/archive/restore, refresh course status and catalogue visibility.
- After progress changes, refresh progress and any affected inactivity state.
- Do not silently keep stale UI state after a confirmed server mutation.

No real-time WebSocket requirement is specified by the PRD. Polling or real-time updates are therefore not required for the UI specification.

---

# 32. State Transition UX

## Course lifecycle

```text
Draft
  └── Publish → Published
Published
  └── Archive → Archived
Archived
  └── Restore → implementation-defined non-archived state
```

The UI should only expose the action that is currently meaningful.

If the server rejects a transition:

- Keep the previous state until confirmation.
- Show the server explanation.
- Refresh state if necessary.

## Learner progress

```text
Not Started
  └── allowed transition → In Progress
In Progress
  └── allowed transition → Completed
```

The exact event/action that advances progress is not fully specified by the PRD. Therefore:

- UI controls may be named according to the implemented backend transition.
- UI must not invent additional progress states.
- UI must not allow skipping or reversing states.
- Server rejection is displayed as authoritative.

---

# 33. UI-Level Security Rules

The interface must:

- Never display learner B's progress to learner A.
- Never provide learner-facing course-edit controls.
- Never provide learner-facing enrollment-for-others controls.
- Never provide learner-facing CSV export controls.
- Never provide learner-facing inactivity alert management.
- Never provide history edit/delete controls.
- Avoid using URL visibility as a substitute for access control.

Direct navigation to a protected route must still result in a server-authorized response.

---

# 34. Implementation Decision Boundaries

The following are intentionally left as implementation decisions because the PRD does not prescribe them:

- Exact color palette.
- Font family.
- Component/UI library.
- Exact desktop/mobile breakpoints.
- Exact course-card vs table presentation where both satisfy the requirement.
- Exact category vocabulary.
- Exact input length constraints.
- Exact file format for bulk upload unless constrained by the backend contract.
- Exact restore destination for Archived courses.
- Exact mechanism by which lesson activity advances course progress.
- Exact numeric progress percentage, because the required domain specifies states rather than a percentage.
- Exact dashboard chart library.
- Exact ordering of activity entries.
- Exact learner landing page after login.

These decisions must not introduce new business requirements.

---

# 35. Definition of UI/UX Done

The UI/UX specification is satisfied when the implemented application provides:

- [ ] Email/password login for both roles.
- [ ] Role-appropriate navigation.
- [ ] Instructor dashboard with all four headline metrics.
- [ ] Enrollment breakdown by course.
- [ ] Enrollment/progress breakdown by state.
- [ ] Eight-week completion chart.
- [ ] Course catalogue for each role with correct visibility.
- [ ] Server-backed search by title/description.
- [ ] Server-backed category/status/instructor filters.
- [ ] Server-backed title/creation-date/enrollment-count sorting.
- [ ] Server-backed pagination and total match count.
- [ ] Instructor course creation/editing.
- [ ] Course lifecycle actions with visible status.
- [ ] Empty-course publish error handling.
- [ ] Lesson add/edit/reorder/remove UI.
- [ ] Ordered lesson learning view.
- [ ] Learner self-enrollment.
- [ ] Instructor single-learner enrollment.
- [ ] Bulk enrollment paste/upload UI.
- [ ] Per-address bulk result table with the three required outcomes.
- [ ] Learner My Courses view with progress.
- [ ] Instructor learner-progress view.
- [ ] CSV export action.
- [ ] Immutable activity history.
- [ ] Activity comment form.
- [ ] Instructor inactivity alert list.
- [ ] Alert count badge.
- [ ] Alert dismissal.
- [ ] Correct empty/loading/error/success states.
- [ ] UI behavior that reflects, but does not replace, server-side authorization.
- [ ] No optional stretch-feature UI added before mandatory functionality is complete.

---

# 36. Source and Scope Note

This specification is based on the approved Product Requirements Document and the corresponding Business Requirements Document for Assignment 05 — Course Delivery & Enrollment.

Where the PRD defines behavior but not a literal UI decision, this document identifies the choice as an implementation decision rather than presenting it as a business requirement.

The ten mandatory goals remain the product boundary.
