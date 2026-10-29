# PeopleDesk — Employee Management System

A Full Stack Java examination project by **Bhagyashree**. PeopleDesk is an administrator-only web application for organizing employee records, with a responsive dark interface and a real MySQL database.

## Features

- Administrator login/logout with authenticated sessions.
- Dashboard with employee counts, departments, monthly salary totals, and recent joiners.
- Add, view, edit, and delete employees, with deletion confirmation.
- Search by employee name, ID, or email and filter by department.
- CSV export of the filtered employee list.
- Twenty-four fictional sample employees for demonstration.
- Responsive desktop and mobile layouts.

## Technology

HTML, CSS, JavaScript → JSON/HTTP → Java Servlets → JDBC → MySQL.

The backend uses embedded Apache Tomcat 10.1, Java 17+, Maven, Gson, and MySQL Connector/J. Passwords use salted PBKDF2 hashing. SQL queries use prepared statements. Employee mutations require a session and CSRF token. JavaScript renders the interface; JSP is not used.

## Quick frontend preview

Open `frontend/index.html` directly in a modern browser. Preview-only login: **admin / demo123**. This mode uses browser storage and is separate from MySQL. These preview credentials do not grant access to the Java backend.

## Run the complete application on Windows

From PowerShell in the project directory:

```powershell
.\setup-tools.ps1
.\setup-mysql.ps1
.\start-website.ps1
```

The setup scripts download portable Java, Maven, and MySQL from official distribution endpoints into `.tools`. The start script initializes a project-local database and prompts for an administrator password on a new installation. Keep the terminal open, visit **http://localhost:8080**, and sign in as **admin** with the password you chose.

Database passwords are generated automatically; local credentials are encrypted using Windows DPAPI and excluded from Git. Existing databases retain their administrator account; selecting a different startup password does not reset it.

To load the fictional records, open a second PowerShell window in the project directory:

```powershell
.\import-demo-employees.ps1
```

See [START-HERE.md](START-HERE.md) for phone access over Wi-Fi, startup, and safe shutdown. For other operating systems or an existing MySQL installation, see [backend/README.md](backend/README.md).

## Tests

```powershell
.\test-backend.ps1
```

The five automated tests cover password hashing, administrator setup, validation, JDBC CRUD, duplicate email rejection, sessions, CSRF protection, and the API lifecycle using a temporary H2 database. A separate real MySQL check is available while the application is running:

```powershell
.\verify-mysql.ps1
```

That check creates a disposable record, edits it, restarts this project's MySQL server, verifies persistence, and deletes the test record. It briefly interrupts database access. Both checks were run successfully during project development.

## Repository layout

```text
frontend/                HTML, CSS, and JavaScript
backend/src/main/java/   Java server, models, validation, and JDBC access
backend/src/test/java/   Automated tests
backend/database/        SQL setup and fictional sample records
*.ps1                    Windows setup and run helpers
```

Downloaded runtimes, database contents, local credentials, logs, and build output are intentionally excluded. Only source code and fictional samples belong in this repository.

## Sharing

This repository shares the source code. GitHub Pages cannot run the Java/MySQL backend. A public live application requires separate Java hosting, a hosted database, HTTPS, and deployment configuration. Attendance, leave management, and payroll calculations are future scope.
