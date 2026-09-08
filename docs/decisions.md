# Decisions

Log the decisions that actually shaped this codebase — the ones where a real alternative existed and
you picked one. At least five entries. For each: what you chose, what you rejected, and why. At least
one entry must be a decision you later reversed — say what changed your mind. It can be any entry
below, not necessarily the last one; add a **Later reversed:** line to whichever one it is.

## Decision 1

* **Chose:**
  ChatGPT for documentation, initial schema design, debugging, and boilerplate code generation.

* **Rejected:**
  Gemini CLI for documentation, initial schema design, and conceptual discussions.

* **Why:**
  ChatGPT provided better support for understanding requirements, designing the initial database schema, explaining technical concepts, and debugging issues interactively. However, using a chatbot for large-scale implementation required repeatedly copying and pasting generated code into the project, which became time-consuming and increased the chances of integration errors.

## Decision 2

* **Chose:**
  Gemini CLI for code generation and implementation.

* **Rejected:**
  ChatGPT as the primary tool for direct codebase implementation.

* **Why:**
  Gemini CLI can work directly within the project repository, allowing it to inspect the existing codebase, understand the project structure, create and modify multiple files, and make changes without repeatedly copying and pasting code between the chatbot and IDE. This made it more efficient for implementing larger features and maintaining consistency with the existing codebase. It also allowed the project to be developed incrementally while working with the existing files and configuration.

## Decision 3

* **Chose:**
  Build the application incrementally, implementing one feature or layer at a time and debugging it before moving to the next feature.

* **Rejected:**
  Building the entire backend and frontend in one pass and debugging everything only after the implementation was complete.

* **Why:**
  The project contains several dependent layers, including database entities, repositories, services, DTOs, controllers, security, APIs, and the React frontend. Implementing everything at once made it difficult to identify the source of errors. Incremental development made problems such as DTO mismatches, JPQL attribute errors, authentication issues, CORS problems, and frontend/backend contract mismatches easier to isolate and fix.

## Decision 4

* **Chose:**
  Use DTOs between the backend entities and the API instead of directly exposing JPA entities through the REST controllers.

* **Rejected:**
  Returning JPA entities directly from the controllers and using the entity classes as the API request/response models.

* **Why:**
  Separating entities from API DTOs provides better control over the API contract and prevents internal database structures from being unnecessarily exposed to the frontend. It also made it easier to shape responses specifically for pages such as the dashboard, course details, progress tracking, and lesson views. During development, this separation also made API contract mismatches visible and easier to correct.

## Decision 5

* **Chose:**
  Use JWT-based authentication with Spring Security and role-based authorization for learners and instructors.

* **Rejected:**
  Using session-based authentication or implementing authentication manually inside individual controllers.

* **Why:**
  The application is a REST API consumed by a separate React frontend, making stateless JWT authentication a better fit. Spring Security also provides a structured way to protect endpoints and restrict functionality based on user roles. This allows learners and instructors to have different permissions while keeping authorization logic centralized rather than duplicating authentication checks throughout the controllers.

## Decision 6

* **Chose:**
  Keep the existing `Lesson.content` database field as a `String`, but change its meaning from storing lesson text to storing the location of an uploaded lesson file.

* **Rejected:**
  Creating a separate database table containing binary file data or changing the lesson content column to a database-specific BLOB type.

* **Why:**
  The actual lesson files are better stored outside the database because PDFs and PowerPoint presentations can be relatively large. Keeping only the file location in the database keeps the database lightweight and makes the storage implementation replaceable. For development, files can be stored in the backend's `uploads` directory, while production can later use object storage such as AWS S3. This also avoids requiring a database migration just to change the storage mechanism.

## Decision 7

* **Chose:**
  Store uploaded lesson files locally in a backend `uploads` directory during development and isolate the storage logic behind a storage service.

* **Rejected:**
  Connecting the application directly to AWS S3 during the initial development phase.

* **Why:**
  Local file storage removes the need for cloud credentials and external infrastructure while the feature is being developed and tested. At the same time, placing file operations behind a dedicated storage service keeps the rest of the application independent of the storage provider. This makes it possible to replace the local implementation with an AWS S3 implementation later without redesigning the lesson APIs or database.

## Decision 8

* **Chose:**
  Support PDF, PPT, and PPTX as lesson file formats and upload them using `multipart/form-data`.

* **Rejected:**
  Sending lesson files as Base64-encoded strings inside normal JSON requests or supporting arbitrary file types.

* **Why:**
  `multipart/form-data` is the standard approach for uploading binary files through a REST API and avoids the unnecessary size overhead of Base64 encoding. Restricting uploads to the formats required by the application also reduces the risk of unsupported or inappropriate files being uploaded. The approach fits naturally with the existing lesson creation and update APIs.

## Decision 9

* **Chose:**
  Keep the frontend and backend as separate applications communicating through REST APIs.

* **Rejected:**
  Serving the React application directly from the Spring Boot application and tightly coupling the frontend to backend views.

* **Why:**
  A separate React frontend provides a cleaner separation of responsibilities and allows the frontend and backend to be developed and tested independently. It also makes the backend API reusable for other clients in the future. This architecture required configuring CORS and handling authentication across the two applications, but those concerns could be addressed centrally.

## Decision 10

* **Chose:**
  Use a centralized API layer on the React frontend for communicating with the backend.

* **Rejected:**
  Making raw Axios/fetch requests directly from every React page and component.

* **Why:**
  Centralizing API calls makes the frontend easier to maintain and keeps endpoint definitions, request handling, and error processing consistent. It also helped when the backend API contracts changed, because endpoint changes could be handled in the API layer rather than being duplicated across multiple pages. This became particularly useful while fixing frontend/backend DTO mismatches and progress-related API issues.


## Decision 11

* **Chose:**
  Add default seeded learner and instructor accounts for development and testing.

* **Rejected:**
  Requiring users to manually register accounts before every development or testing session.

* **Why:**
  The application has role-dependent functionality, so having a default learner and instructor makes it much easier to test authorization, course creation, enrollment, progress tracking, and instructor functionality consistently. The seeded accounts are intended for development/testing rather than production credentials.

## Decision 12

* **Chose:**
  Debug backend and frontend issues by tracing the complete request/response flow rather than fixing only the visible error.

* **Rejected:**
  Applying isolated changes only to the file where an error appeared.

* **Why:**
  Several issues encountered during development crossed application boundaries. For example, a frontend error could originate from a backend DTO mismatch, a JPQL query could fail because it referenced an incorrect entity attribute, and CORS/security errors could prevent an otherwise correct API request from reaching the controller. Following the request from the React component through the API, controller, service, repository, and database made these problems easier to identify and prevented superficial fixes.

## Decision 13

* **Chose:**
  Use a relative/portable file location in the database rather than storing an absolute operating-system-specific path.

* **Rejected:**
  Storing paths such as `C:\project\uploads\lessons\file.pdf` directly in the database.

* **Why:**
  Absolute paths are tied to a particular machine and would break when the application is moved to another development machine, server, Docker container, or cloud environment. A portable location such as `/uploads/lessons/file.pdf` keeps the database independent of the underlying filesystem and makes the eventual migration to cloud object storage easier.

## Decision 14

* **Chose:**
  Use UUID-based filenames for uploaded lesson files.

* **Rejected:**
  Saving uploaded files using the original filename supplied by the user.

* **Why:**
  Different users may upload files with the same name, and relying on the original filename could cause files to overwrite each other. UUID-based filenames provide a unique identifier for each uploaded file while the original filename can still be used as presentation metadata if needed. This also reduces problems caused by duplicate filenames and special characters.

## Decision 15

* **Chose:**
  Reuse the existing lesson model and API concept while extending lessons to support file-based content.

* **Rejected:**
  Creating a completely separate `DocumentLesson` or `PresentationLesson` model and API.

* **Why:**
  From the application's perspective, a PDF or PowerPoint presentation is still a lesson. Creating separate models would introduce unnecessary duplication into the course, progress, enrollment, and frontend logic. Extending the existing lesson concept allows existing lesson ordering and progress functionality to continue working while changing how the lesson content is stored and delivered.


## Decision 16

* **Chose:**
  Initially use AI-generated implementation followed by repeated testing, debugging, and refinement.

* **Rejected:**
  Expecting the first generated implementation to work without iterative testing.

* **Why:**
  The project contains many interacting components, so generated code often required adjustment to match the existing project structure and API contracts. Testing exposed issues such as incorrect entity attributes in JPQL queries, DTO mismatches, CORS/security configuration problems, and frontend component errors. Treating AI generation as an iterative development process rather than a one-shot solution produced a more reliable result.

# **Later reversed:**
Initially, lessons were implemented as text-based content, where the instructor had to manually enter the lesson content as a string. As the project requirements evolved, we decided that lessons should support actual learning materials such as PDF and PowerPoint files. The lesson model was therefore extended so that the existing string field stores the location of the uploaded file instead of the lesson text. Files are stored in the backend's uploads folder during development, with the design allowing the storage implementation to be replaced by cloud storage such as AWS S3 in production.

At the same time, our initial approach of having ChatGPT generate complete implementation code and manually integrating it into the project was later reduced in favor of using Gemini CLI for repository-level implementation. The change was made because direct repository access made it easier to inspect the existing codebase, modify multiple related files consistently, and reduce repetitive copy-pasting and integration errors.
