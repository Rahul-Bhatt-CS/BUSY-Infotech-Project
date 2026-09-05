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
  Gemini CLI can work directly within the project repository, allowing it to inspect the existing codebase, understand the project structure, create and modify multiple files, and make changes without repeatedly copying and pasting code between the chatbot and IDE. This makes it more efficient for implementing larger features and maintaining consistency with the existing codebase. It can also work with the project's existing files and configuration while developing features incrementally.


## Decision 3

- **Chose:**
- **Rejected:**
- **Why:**

## Decision 4

- **Chose:**
- **Rejected:**
- **Why:**

## Decision 5

- **Chose:**
- **Rejected:**
- **Why:**
