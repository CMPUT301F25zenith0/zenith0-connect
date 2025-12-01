[⬅️Back](../../Connect-Docs.md)
# 🏗️ System Architecture Overview

Welcome to the **Architecture** documentation for this application.  
This section describes the **backend design**, **database schema**, **data flow**, and other structural components that make up the system.

---

## 📋 Table of Contents
- [Overview](#overview)
- [Backend](#backend)
- [Database Schema](#database-schema)
- [System Flow](#system-flow)
- [CRC Cards](#crc-cards)


---

## 🧩 Overview

This document provides a high-level overview of how different parts of the application interact, the technologies used, and how data moves through the system. The architecture best follows software engineering principles, emphasizing modularity, separation of concerns, and maintainable code structures, and code comments. 

The system leverages consistent UI patterns, centralized resource management, and structured data handling to promote long-term scalability. Core components follow clear responsibilities views handle presentation, controllers manage logic, and data layers communicate with Firestore in a predictable and secure manner. These practices ensure reliability, reduce technical debt, and support smooth collaboration across the development team.

> 💡 *For details on individual components, refer to the linked documents below.*


---

## ⚙️ Backend Design

The backend is responsible for handling API requests, managing business logic, and interacting with the database.

- **Java, XML, Android Studio Enviornment, Firebase**
- **Key Modules:**
  - Authentication & Authorization storing users in firebase
  - Data Processing / Filtering Logic through firebase built-in functions
  - Firebare indexing to collection group information through entire Database
  - UI designs / Responsive screen using XML. E.g Material buttons, Image buttons, colour animations etc

  📄 **Backend Design:**  
➡️ [UML](./UML%20Diagrams.pdf)
➡️ [Description](./UML%20Diagram%20Documentation.pdf)



---

<!-- Need to check and edit this entire page-->
## 🗄️ Database Schema

This section outlines the data storage design, including entities, relationships, and schema diagrams.

- **Database Type:** _e.g. Firebase_
- **Main Collections:**
  - Users
  - Events
  - Waitlists
  - Notifications

📄 **Detailed Schema:**  
➡️ [Database Schema](./firedraw-schema.png)

---

## 🧱 CRC Cards

Class–Responsibility–Collaborator (CRC) cards define the roles of classes and their relationships.

📄 **View CRC Cards:**  
➡️ [CRC Cards](./crc.md)

---

_Last updated: [2025-12-1]_
