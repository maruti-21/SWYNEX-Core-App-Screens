# FocusFlow — Core App Screens

## SWYNEX Technologies Internship — Task 2

FocusFlow is a productivity and focus-management Android application designed to help users organize tasks, track progress, and maintain focused work sessions.

---

## 📌 Task 2 — Core App Screens

### Task Objective

The objective of Task 2 is to build the primary screens of the FocusFlow application and provide a functional, reviewable mobile interface.

---

## 💡 Application

### FocusFlow

FocusFlow combines task management, progress tracking, focused work sessions, and application settings into a single Android application.

The core screens developed for this task are based on the application concept and screen flow defined in Task 1.

---

## 📱 Core Screens

### 1. Dashboard

The Dashboard acts as the main screen of the application.

Features include:

- Personalized greeting
- Today's progress
- Task information
- Add Task action
- Focus Timer access
- Progress access
- Settings access

---

### 2. Add/Edit Task

Allows users to create and modify tasks.

Features include:

- Task title
- Description
- Priority
- Category
- Due-date information
- Save/update task

Available categories:

- Study
- Work
- Personal
- Other

---

### 3. Task Details

Displays complete information about a selected task.

Users can:

- View task information
- Edit the task
- Mark the task as completed
- Delete the task

---

### 4. Progress

Displays task completion and productivity information.

Users can use this screen to understand their current task progress.

---

### 5. Focus Timer

Provides a dedicated focused-work session.

Features include:

- Focus duration selection
- Countdown timer
- Start/stop controls
- Focus session completion

---

### 6. Settings

Provides application preferences and controls.

Features include:

- User name
- Notification settings
- Focus duration
- Task-data controls
- Application information

---

## 🔄 Screen Navigation

```text
                         Dashboard
                            │
          ┌─────────────────┼─────────────────┐
          │                 │                 │
          ▼                 ▼                 ▼
     Add/Edit Task       Progress        Focus Timer
          │
          ▼
     Task Details

                            │
                            ▼
                         Settings
