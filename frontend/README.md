# PeopleDesk — Employee Management Frontend

Created for Bhagyashree's Full Stack Java examination project.

## Open the website

Double-click `index.html` in this folder to open it in a modern browser. No Java, MySQL, build command, or installation is needed for this frontend preview. Internet is optional; Google Fonts falls back to system fonts when offline.

The login page opens first. Sign in to reach the dashboard. Demo username: `admin`. Demo password: `demo123`. Click the logout icon at the bottom of the sidebar to return to login.

## Try the features

- View employee, department, salary, and joining-date statistics.
- Switch between Overview and Employees.
- Search by name, ID (such as EMP-1001), or email; filter by department.
- Add and edit employees using the form. Duplicate email addresses are rejected.
- Delete an employee after confirmation.
- Export the currently filtered employee list as CSV.
- Resize the browser to use the mobile layout.

Twenty-four fictional records across six departments are included. Existing browser demos receive the sixteen additional samples once, preserving user records and avoiding ID collisions. Employee changes persist in this browser's local storage when available. They are not shared between computers or browsers. If browser storage is unavailable, a notice identifies temporary data. Do not enter real confidential employee data into this prototype.

## Files

- `index.html`: page structure, forms, and dialogs.
- `styles.css`: colors, typography, layout, and responsive styles.
- `app.js`: frontend interactions and demo browser storage.

## Java integration

Opening this file directly is a frontend prototype. Demo credentials in JavaScript do not provide secure authentication. The adjacent `backend` directory now contains the Java Servlet/JDBC/MySQL implementation and setup guide. When served through Java at `http://localhost:8080`, this frontend uses real login, authenticated API requests, and database storage. A generic static HTTP server does not provide this API. See the backend README for requirements and verification status.
