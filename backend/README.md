# Java + MySQL backend

This stage connects PeopleDesk's existing HTML/CSS/JavaScript frontend to Java Servlets and JDBC. An embedded Apache Tomcat runs the application, so a separate Tomcat installation is unnecessary. No JSP is used because the existing frontend renders JSON responses directly.

## Prerequisites

- JDK 17 or newer (JDK 21 is suitable).
- Apache Maven 3.9 or newer.
- MySQL Server 8.0 or newer. Workbench is an optional GUI; Workbench alone does not install the server.

## Setup

For the project-local installation, run `start-website.ps1`, then open **http://localhost:8080**. Sign in as **admin** with your configured password. A fresh installation prompts for a password; existing installations retain theirs. The project-local MySQL database runs on port 3307. See `../START-HERE.md` for start/stop instructions. The manual setup below is for a separate MySQL installation.

The optional root `setup-tools.ps1` downloads checksum-verified portable Java and Maven into `.tools`, without changing the system PATH. If using these tools, configure the environment variables below and run `..\start-backend.ps1` from the backend directory instead of `mvn compile exec:java`.

1. Open `database/setup.sql` in MySQL Workbench as a database administrator. Replace `REPLACE_WITH_YOUR_DATABASE_PASSWORD` with your own password, then run the script once. If the database user already exists, change its password separately with `ALTER USER` rather than assuming this script resets it.
2. Open PowerShell in `C:\employee system\backend`.
3. Configure the session. Replace the example passwords before running; do not commit credentials or put them in screenshots.

```powershell
$env:DB_USER = 'peopledesk'
$env:DB_PASSWORD = 'your-database-password'
$env:ADMIN_USERNAME = 'admin'
$env:ADMIN_PASSWORD = 'your-admin-password-at-least-12-characters'
mvn test
mvn compile exec:java
```

4. Open **http://localhost:8080** and sign in with the administrator credentials you set above.
5. Add an employee, edit it, search for it, and reload the page. Stop and restart Java to confirm database persistence. Test deletion and logout.

The first startup creates the tables and a hashed admin account. Later startups reuse that account; changing `ADMIN_PASSWORD` does not reset an existing account. An empty database starts with zero employees. Prototype browser records are never silently copied to MySQL.

The service binds to localhost for a local exam demonstration. Stop it with Ctrl+C. A public deployment would require HTTPS and deployment-specific configuration.

## Optional environment settings

`DB_URL` defaults to `jdbc:mysql://localhost:3306/employee_management?connectionTimeZone=Asia/Kolkata`. `PORT` defaults to 8080. `FRONTEND_DIR` defaults to `../frontend`, relative to the backend working directory.

## Project structure and explanation

- `Application.java`: starts Tomcat and serves the frontend and `/api/*`.
- `ApiServlet.java`: handles JSON requests, sessions, CSRF checks, and HTTP errors.
- `Employee.java`: employee model and server validation.
- `EmployeeDao.java`: prepared SQL statements for CRUD; closes JDBC resources.
- `Database.java`: database connections, schema creation, admin lookup.
- `Passwords.java`: salted PBKDF2 password hashing.

Flow: browser → Java Servlet → DAO → JDBC → MySQL → JSON → browser.

## API

POST `/api/login`, GET `/api/session`, POST `/api/logout`, GET/POST `/api/employees`, PUT/DELETE `/api/employees/{id}`. All employee routes require an authenticated session. Mutations also require the `X-CSRF-Token` returned by login/session. Sessions expire after 30 minutes of inactivity.

## Tests and limitations

`mvn test` checks password hashing, initial admin creation, invalid input, SQL parameterization for authentication, duplicate-email rejection, and employee create/read/update/delete using H2 in MySQL compatibility mode. H2 tests do not prove MySQL compatibility; a real MySQL demonstration must also be checked.

Verified on 29 October 2026: Java compilation succeeded and all five tests passed, including the HTTP API lifecycle test covering unauthenticated access, login, CSRF protection, create/read/update/delete, duplicate email, and logout. Frontend JavaScript syntax also passed. A separate real MySQL 8.4.13 check passed through the running Java API: login, create/read/update/delete, and retention of records after a MySQL restart. All 24 fictional employees remain in the database, and temporary verification records were removed. The website returned HTTP 200. The browser preview could open the login page, but its tabs closed unexpectedly before an interactive browser login could be verified. Use `../verify-mysql.ps1` to repeat the real database check and `../test-backend.ps1` for the temporary-database tests. Localhost connections must be permitted.

Opening `frontend/index.html` directly still uses the standalone browser demo. Opening through Java at `http://localhost:8080` uses the real API and requires login. A generic static HTTP server will not provide the Java API.
