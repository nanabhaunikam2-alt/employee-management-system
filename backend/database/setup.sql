-- Run in MySQL Workbench as a database administrator.
CREATE DATABASE IF NOT EXISTS employee_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- Replace the placeholder before running. Use the same password in DB_PASSWORD.
CREATE USER IF NOT EXISTS 'peopledesk'@'localhost' IDENTIFIED BY 'REPLACE_WITH_YOUR_DATABASE_PASSWORD';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE ON employee_management.* TO 'peopledesk'@'localhost';
-- Java creates the tables and the initial admin account on first startup.
