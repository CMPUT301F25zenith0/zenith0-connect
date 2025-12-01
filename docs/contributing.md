[⬅️Back](../Connect-Docs.md)
# 👥 Team Contributions

This page outlines the individual contributions made by each team member and links to related project documentation such as meeting notes and progress records.

---
<!-- Check if needs edits -->

## 🧑‍💻 Team Members & Contributions

### 1. Digaant Chokkra
**Role:**  _DevOps / Backend Developer_

**Contributions:**
- Implemented CreateEvent activity with comprehensive date/time validation, geolocation toggle, registration window management, waiting list capacity controls, and event labeling system with 15+ predefined categories as well ability to Edit events and upload pictures. 
- Developed OrganiserActivity with event filtering (all/open/closed/drawn), real-time Firebase integration
- Implemented QR code generation for event sharing and automated lottery draw functionality with random selection, capacity limits, and status tracking
- Created 100+ unit tests across and model tests covering business logic and edge cases
- Designed Material Design layouts for organizer interfaces; conducted systematic debugging to resolve Firebase synchronization and integration issues


---

### 2. Sai Vashnavi Jattu
**Role:**  _Backend Developer / DevOps / System Designer_

**Contributions:**
- Designed and implemented event filtering, enabling entrants to find suitable events efficiently.
- Co-developed the admin section, including backend and UI flows for browsing, managing, and deleting events, profiles, and images.
- Implemented geolocation features for geo-verified joins and organizer entrant maps, integrating location services with Firebase.
- Performed UI and functional testing (including end-to-end flows and edge-case checks such as no location/network and empty filters) and contributed to the project’s CRC cards and UML diagrams.

---

### 3. Jagjot Brar
**Role:**  _Frontend Designer / UI Designer / Backend Developer_

**Contributions:**
-Designed the user flow that the team maintained throughout the final UI design.
- Created foundational wireframes and temporary UI layouts to guide development.
- Organized UML and system flow diagrams to support clear and consistent implementation.
- Ensured coherent navigation and flow across all app activities.


---

### 4. Alpesh Dayal
**Role:** _UI Designer / Backend Developer_

**Contributions:**
- Contributed to the event registration and lottery draw system, including manual trigger logic, deadline checks, entrant fetching, and Firestore integration.

- Implemented the notification system overhaul, including accept/decline migration to My Events, database updates on close, and adapter-level UI/logic fixes.

- Developed and refined multiple custom UI screens using ConstraintLayout (events, notifications, settings), ensuring consistent styling, button pairing, and responsiveness across the app.

- Debugged core features across the project — fixing Firestore index errors, deprecated APIs, unresponsive views, and logic bugs — while maintaining clean GitHub commits and merges.

---

### 5. Vansh Taneja
**Role:**  _UX Specialist / UI Designer / Backend Developer_

**Contributions:**
- Authentication and user session management — Implemented auto-login and "Remember Me" with SharedPreferences
- Geographic data visualization — Built the entrant map feature using Google Maps to display where users joined waiting lists, with batch data loading and statistics.
- Admin dashboard and management — Co-developed the admin dashboard with user statistics and active user tracking, plus multiple admin management screens for events, profiles, images, and organizers.
- UI components and testing — Created image display functionality and wrote unit tests to ensure code quality and reliability.


---

### 6. Aakansh Chatterjee
**Role:** _Project and Documentation Lead / UI Designer / Backend Developer_

**Contributions:**
- Handled task selection and distibution
- Built and managed documentation to display most up-to-date information of completed work
- Implemented Firebase Authentication logic, including credential sign-in and user status verification (admin, disabled)
- Built QR Code Scanning system, handling camera permissions and custom deep-link URI parsing (myapp://event/signup)
- Built the event Report system for users, nose-to-tail 
- Contributed significantly to the Admin Dashboard and the complex, cascading Organizer Soft-Delete feature. E.g Organizer and profile deletion
- Implemented the live active user tracking system for admin dashboard
- Built the dynamic Event Label/Category Chip functionality.
- Co-authored the critical, multi-step Cascading Organizer Soft-Delete transaction logic for multiple files
- Debugged large features and fixed bugs

---

## 🗂️ Related Pages & Project Logs

| Document | Description |
|-----------|-------------|
| [📝 Meetings ](meetings.md) | Regular meetings to go over project details, update progress, and plan out sprints |
| [🧾 Development Log](https://github.com/CMPUT301F25zenith0/zenith0-connect/tree/main) | Github Development Logs |
| [🖥️ Video](https://drive.google.com/file/d/1_33SiO3VnoRURQ9TR36zNscetIjABec1/view) | App Demonstration Video|
| [📝 Additonal Logs](https://drive.google.com/drive/folders/1bG_5UgYcSS8Khb-tzAWPUlYHnTzYOVVN?usp=sharing) | Google Documents|

--- 

> _This page is continually updated as development progresses and new contributions are made._

_Last updated: [2025-12-01]_