# Submission

Fill this in and commit it. This is the first file we open.

## Links

- **GitHub repository:**
Backend: https://github.com/Rahul-Bhatt-CS/BUSY-Infotech-Project
Frontend: https://github.com/Rahul-Bhatt-CS/BUSY-Infotech-Project-Frontend
- **Live application:** https://busy-infotech-project-frontend.vercel.app/

## Notes for the reviewer

The frontend is deployed on Vercel and the backend is deployed on Render. The backend may take some time to respond if the Render service has been idle. A GitHub Actions keep-alive workflow has been added to reduce this issue.

The MySQL database is hosted on Aiven.

### Deployment Note

The `Aiven MySQL` instance may occasionally become unavailable and require a manual restart. If requests continue to fail after waiting for some time, please contact me at **[rbhatt.cs@gmail.com](mailto:rbhatt.cs@gmail.com)** or **+91 8630984049**.
 

## Demo credentials

| Role       | Email                        | Password         |
| ---------- | ---------------------------- | ---------------- |
| Instructor | `instructor@learnwithus.com` | `Instructor@123` |
| Learner    | `learner@learnwithus.com`    | `Learner@123`    |

There are also 9 additional seeded learner accounts (`learner1@learnwithus.com` to `learner9@learnwithus.com`) using the same learner password.

## Stack

| Layer    | What you used           | Why                                                            |
| -------- | ----------------------- | -------------------------------------------------------------- |
| Frontend | React + TypeScript      | Component-based UI with type safety                            |
| Backend  | Java + Spring Boot      | REST APIs, business logic, security and database access        |
| Database | MySQL on Aiven          | Relational data with clear relationships and constraints       |
| Hosting  | Vercel + Render + Aiven | Simple deployment of frontend, backend and database separately |

## Goal checklist

| #  | Goal                           | Status | Notes                                                                                                                                                 |
| -- | ------------------------------ | ------ | ----------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1  | Accounts and roles             | Done   | JWT authentication with separate learner and instructor roles. Authorization is enforced on the server.                                               |
| 2  | Courses                        | Done   | Instructors can create, edit, publish, archive and restore courses.                                                                                   |
| 3  | Lessons inside courses         | Done   | Instructors can add, edit, reorder and remove lessons. PDF and PPT uploads are also supported.                                                        |
| 4  | Course and progress states     | Done   | Course and learner progress states are enforced by the backend, including blocking invalid transitions and publishing empty courses.                  |
| 5  | Enrollment                     | Done   | Learners can self-enrol and instructors can enrol learners. Bulk enrollment is also supported.                                                        |
| 6  | Finding courses                | Done   | Server-side search, filtering, sorting and pagination are implemented.                                                                                |
| 7  | Bulk enrollment and CSV export | Done   | Bulk enrollment reports unknown, already-enrolled and newly-enrolled addresses, and course progress can be exported as CSV.                           |
| 8  | Dashboard                      | Done   | Dashboard includes learner/course counts, completions, in-progress learners, course breakdowns and completion trends.                                 |
| 9  | History and comments           | Done   | Course activity and comments are recorded with the actor and cannot be edited or deleted.                                                             |
| 10 | Inactivity alerts              | Done   | Learners inactive for more than 14 days while in progress appear as instructor alerts, with dismissed alerts able to reappear after renewed activity. |

## How much time did you actually spend?

I worked on the project around `16 hours` in total, but the hours were `not evenly distributed`. On the first day I had limited time and mainly went through the documentation and initial setup. The following day was Sunday, so I used the extra time to compensate and complete more work.

After that, I adjusted the amount of work according to my college class schedule, doing larger features on days when I had more time and smaller fixes or documentation on busier days.

## What would you do next, with another 12 hours?

I would mainly focus on polishing and improving the existing system and adding some new feature if possible.

My priorities would be:

1. Improve frontend UI/UX and consistency.
2. Add Object Storage like AWS S3 for file storage in lessons
3. Optimize the N+1 database queries.
4. Improving the CSV report export UI.
5. Do another complete end-to-end testing pass on the deployed application.

## What are you least happy with in this codebase, and why?

I am least happy with some of the database query patterns and the current file-storage approach.

Some responses can result in multiple database queries, which is acceptable for the current scale but would become inefficient with much more data. The lesson files are also stored on the backend's local filesystem, which is not ideal if the backend is eventually scaled across multiple instances.

These are areas I would improve next because the current implementation works, but they would become important as the application grows.
