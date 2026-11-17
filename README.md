# EduQuiz - Learn . Attempt . Improve

![Platform](https://img.shields.io/badge/Platform-Android-brightgreen)
![Language](https://img.shields.io/badge/Language-Java-blue)
![Backend](https://img.shields.io/badge/Backend-Firebase-orange)
![Min SDK](https://img.shields.io/badge/Min%20SDK-API%2024-informational)

EduQuiz is an Android quiz application that brings quiz creation, quiz attempt, automatic evaluation and performance tracking into one cloud-backed app. It has two panels: a **Student Panel** for attempting quizzes and tracking progress, and an **Admin Panel** for creating, publishing and managing quiz content. A quiz published by an administrator becomes available to students immediately.

This project was developed as a Master of Computer Applications (MCA) project at the Department of Computational Sciences, Brainware University, Kolkata.

---

## Table of Contents
1. [Problem Statement](#problem-statement)
2. [Solution Overview](#solution-overview)
3. [Features](#features)
4. [Tech Stack](#tech-stack)
5. [System Architecture](#system-architecture)
6. [Database Structure](#database-structure)
7. [Security](#security)
8. [Core Rules and Calculations](#core-rules-and-calculations)
9. [App Screens](#app-screens)
10. [Design System](#design-system)
11. [Getting Started](#getting-started)
12. [Usage](#usage)
13. [Future Scope](#future-scope)
14. [Team](#team)
15. [Acknowledgement](#acknowledgement)
16. [License](#license)

---

## Problem Statement

Practice tests are traditionally prepared, distributed and checked by hand. This causes several problems:

- Manual preparation and evaluation are slow and error-prone.
- Students wait days for feedback, by which time they have forgotten the topic.
- Scores are not recorded in one place, so there is no view of subject-wise accuracy or improvement over time.
- Practice questions are scattered across books, chat groups and websites.
- Teachers lack a simple tool to build quizzes, publish them when ready and read student feedback.
- Shared answer keys leak easily and results cannot be trusted.

## Solution Overview

EduQuiz automates the whole cycle. Administrators build a question bank and publish quizzes. Students attempt them, get instant results, and see their performance calculated from real attempt records. All data lives in Firebase, with role-based access enforced by Firebase Security Rules.

**Target users:** school and college students, competitive-exam aspirants, teachers and institutions, and self-learners.

## Features

### Student Panel
- Secure registration and login with Firebase Authentication
- Dashboard with study streak, total quizzes attempted and overall accuracy
- Subject-wise quizzes with a search bar; only published quizzes can be started
- Quiz details screen showing title, subject, description, question count and time limit
- Quiz attempt with Previous, Next and Submit controls, a progress indicator and answer retention while navigating
- Optional countdown timer with automatic submission when time expires
- Confirmation dialog before submitting
- Random quiz built from published questions across all subjects, with no repeated question in one attempt
- Instant result: score percentage, total, correct and wrong answers
- Performance screen: overall accuracy, subject-wise accuracy and quiz history
- Feedback screen with star rating and message
- Profile screen with logout

### Admin Panel
- Separate admin login, accessible only to accounts listed in the `admins` node
- Dashboard showing the number of registered students
- Add subjects with name, description and optional icon
- Create quizzes by selecting a subject and entering title, description, number of questions and an optional time limit
- Add questions with four options (A to D) and a correct-answer selector, with a progress indicator
- Preview a quiz and publish it only when every required question is valid
- Edit or delete subjects and quizzes, with a confirmation dialog before deletion
- Read student feedback with names, ratings, messages and dates

## Tech Stack

| Category | Technology |
|---|---|
| IDE | Android Studio |
| Language | Java |
| UI | XML layouts, Material Design Components |
| View access | ViewBinding |
| Lists | RecyclerView |
| Authentication | Firebase Authentication |
| Database | Firebase Realtime Database |
| Access control | Firebase Security Rules |
| Minimum Android version | Android 7.0 (API 24) |

Jetpack Compose and Kotlin are not used.

## System Architecture

EduQuiz follows a three-layer structure:

| Layer | Description |
|---|---|
| Frontend | XML layouts with Java activities and fragments. Each screen has its own layout, and lists use RecyclerView adapters. |
| Backend | Firebase Authentication for sign-up and sign-in, plus Firebase Security Rules for role-based access control. |
| Database | Firebase Realtime Database storing data as JSON nodes with unique IDs. |

The code is divided into separate classes for authentication, student dashboard, admin dashboard, subject management, quiz management, question management, quiz attempt, result calculation, performance, feedback and Firebase operations.

### Application Flow

```
Student:  Login -> Dashboard -> Subject Quiz / Random Quiz -> Attempt -> Result -> Performance

Admin:    Login -> Dashboard -> Add Subject -> Add Quiz -> Add Questions -> Preview -> Publish
                                                                                          |
                                                                          Quiz available to students
```

## Database Structure

| Node | Purpose and main fields |
|---|---|
| `users/` | Student profile: uid, name, email, accuracyPercentage, streak, totalAttempts, totalCorrect, totalWrong, totalQuestions, lastActiveDate, createdAt |
| `admins/` | Admin identity and role: uid, email, name, role (writable only from the Firebase console) |
| `subjects/` | id, name, description, icon, questionCount, quizCount |
| `quizzes/` | id, subjectId, sectionId, subjectName, title, description, totalQuestions, timeLimitMinutes, published, createdAt |
| `questions/` | id, quizId, questionText, optionA, optionB, optionC, optionD, correctAnswer |
| `attempts/` | id, userId, quizId, quizTitle, random, subjectId, subjectName, totalQuestions, correctAnswers, wrongAnswers, score, scorePercentage, dateString, timestamp |
| `feedback/` | id, userId, userName, rating, message, dateString, timestamp |

Every quiz is linked to a subject, every question to a quiz, and every attempt to a student and a quiz.

## Security

- Passwords are handled by Firebase Authentication and are never stored in the app or in plain text in the database.
- A user is an administrator only if their UID exists under the `admins` node. Admin credentials are not embedded in the app.
- Students can read and write only their own profile and attempts.
- Any signed-in user can read subjects, quizzes and questions, but only administrators can create, edit or delete them.
- Any signed-in user can submit feedback, and only administrators can read it.
- The `admins` node cannot be written from the app. Admin accounts are created from the Firebase console.

Example of the admin rule used for content nodes:

```json
"subjects": {
  ".read": "auth != null",
  ".write": "auth != null && root.child('admins').child(auth.uid).exists()"
}
```

## Core Rules and Calculations

- **Accuracy (%)** = (Correct Answers / Total Attempted Questions) x 100
- **Wrong Answers** = Total Attempted Questions - Correct Answers
- **Total Attempted Questions** is the sum of question counts across all submitted quizzes.
- **Study streak** is the number of consecutive calendar days on which the student completed at least one quiz. Several attempts on one day count as one active day.
- **Random quizzes** use only published questions and never repeat a question within one attempt.
- Every attempt is saved with the student ID, quiz ID, selected answers, score and timestamp.

## App Screens

### Student Panel
| Screen | Description |
|---|---|
| Splash | Logo and tagline; routes logged-in students to the dashboard |
| Login / Registration | Forms with validation messages |
| Dashboard | Streak, attempts, accuracy and four quick-access cards with bottom navigation |
| Subject Selection | Searchable subject cards |
| Quiz Details | Quiz information and Start Quiz button |
| Quiz Attempt | Timer, question card, four options, progress bar |
| Result | Score percentage with correct and wrong counts |
| Performance | Accuracy, totals, subject-wise bars and history |
| Feedback | Star rating and message |
| Profile | Name, email and logout |

### Admin Panel
Admin Login, Admin Dashboard, Add Subject, Add Quiz, Add Questions, Publish Quiz, Manage Quizzes and Subjects, and Student Feedback.

> Add screenshots in a `screenshots/` folder and reference them here, for example `![Dashboard](screenshots/student_dashboard.png)`.

## Design System

| Element | Value | Usage |
|---|---|---|
| Primary | `#6236D9` | Primary actions, headers, selected states |
| Secondary | `#8B65EF` | Accents and gradients |
| Background | `#F8F7FC` | Main screen background |
| Card | `#FFFFFF` | Content cards and forms |
| Primary text | `#202033` | Headings |
| Secondary text | `#77758A` | Descriptions and helper labels |
| Success | `#20B67A` | Correct answers |
| Error | `#E65365` | Validation errors and wrong answers |
| Shape | 16-20 dp corners | Cards and containers |

## Getting Started

### Prerequisites
- Android Studio (latest stable version)
- JDK 8 or above
- An Android device or emulator running Android 7.0 (API 24) or higher
- A Firebase project with Authentication and Realtime Database enabled

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/jinatyeasmin/ANDROID-PBL.git
   cd ANDROID-PBL/Android_PBL_EduQuiz
   ```

2. Open the `Android_PBL_EduQuiz` folder in Android Studio and let Gradle sync.

3. Set up Firebase:
   - Create a project in the [Firebase Console](https://console.firebase.google.com/).
   - Add an Android app using the project's package name.
   - Download `google-services.json` and place it in the `app/` folder.
   - Enable **Email/Password** sign-in under Authentication.
   - Create a **Realtime Database** and paste the security rules from the [Security](#security) section.

4. Create an admin account:
   - Register a user in Firebase Authentication.
   - In the Realtime Database, add a record under `admins/<uid>` with `uid`, `email`, `name` and `role`.

5. Run the app with the Run button on a device or emulator.

## Usage

**Student**
1. Register or log in.
2. Choose a subject quiz or a random quiz from the dashboard.
3. Read the quiz details and tap Start Quiz.
4. Answer the questions and submit, or let the timer submit automatically.
5. View your result, then check your performance and history.
6. Send feedback from the Feedback screen.

**Administrator**
1. Log in from the admin login screen.
2. Add a subject, then create a quiz under it.
3. Add every question with four options and the correct answer.
4. Preview the quiz and publish it.
5. Manage content and read student feedback from the dashboard.

## Future Scope

- Server-side answer protection: store correct answers in a protected node and evaluate them with trusted server code such as Firebase Cloud Functions
- Stricter database rules, including server-side validation of attempt scores
- Leaderboards, achievement badges and reminder notifications
- Richer question types (true/false, multiple-answer, image-based) and bulk import from a spreadsheet
- Offline mode and advanced analytics for administrators

## Team

| Name | Roll Number | Student Code |
|---|---|---|
| Kuntal Bera | 25102410031 | BWU/MCA/25/037 |
| Suman Bajani | 25102410033 | BWU/MCA/25/039 |
| Jinat Yeasmin | 25102410036 | BWU/MCA/25/042 |

**Project guide:** Professor Samrat Kundu

## Acknowledgement

We thank our guide, Professor Samrat Kundu, for his guidance and support throughout this project, and Brainware University for giving us the opportunity to build it.

## License

This project was created for academic purposes at Brainware University. Add a `LICENSE` file if you want to make the terms explicit.
