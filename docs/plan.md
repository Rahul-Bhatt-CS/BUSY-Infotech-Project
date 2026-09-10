# Plan

### 1. How did you break the work into sessions?

I broke the work into small feature-based sessions instead of trying to build everything at once.

I started with the project setup and documentation, then moved to the database entities and security, followed by the course APIs and business logic. After the main backend was working, I focused on authorization, bug fixes, file uploads, bulk enrollment, deployment, and finally inactivity alerts and deployment reliability.

### 2. What order did you build in, and why that order?

I built the project from the foundation upward:

1. Project setup and dependencies
2. Schema and initial documentation
3. Entities and JWT security
4. Course repositories, services and APIs
5. Business logic and controllers
6. Authorization for learners and instructors
7. Bug fixes and DTO cleanup
8. File uploads and bulk enrollment
9. Docker and deployment
10. Inactivity alerts and keep-alive setup

I followed this order because the higher-level features depended on the lower-level ones. For example, I needed the entities and security in place before building protected APIs and business rules.

### 3. What did you estimate versus what it actually took?

I did not follow a fixed number of hours each day. On the first day, I had limited time, so I mainly went through the documentation and completed the initial project setup. The following day was Sunday, so I used the extra time to compensate and completed more of the planned work.

After that, I adjusted the amount of work I did each day according to my college class schedule. On days when I had more time, I worked on larger features, while on busier days I focused on smaller tasks, fixes, or documentation.

Overall, the project was completed over roughly one week, but the workload was not evenly distributed across the days.

### 4. What did you cut when you ran short?

I mainly cut or simplified things that were not essential to the core requirements.

Instead of spending time on extra abstractions or advanced optimizations, I focused on getting the main user flows working: authentication, course management, lessons, enrollment, progress, authorization, file uploads, and deployment.

I also removed unnecessary DTOs and extra files once I found they were not needed.

My priority was to deliver a working end-to-end system first and leave deeper performance optimization or additional features for a later iteration.
