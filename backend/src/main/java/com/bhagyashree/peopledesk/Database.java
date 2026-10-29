package com.bhagyashree.peopledesk;

import java.sql.*;

public final class Database {
    private final String url, user, password;
    public Database(String url, String user, String password) { this.url = url; this.user = user; this.password = password; }
    public Connection connect() throws SQLException { return DriverManager.getConnection(url, user, password); }

    public void initialize() throws SQLException {
        try (Connection c = connect(); Statement s = c.createStatement()) {
            s.executeUpdate("CREATE TABLE IF NOT EXISTS admin (admin_id INT PRIMARY KEY AUTO_INCREMENT, username VARCHAR(50) NOT NULL UNIQUE, password_hash VARCHAR(255) NOT NULL)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS employee (employee_id BIGINT PRIMARY KEY AUTO_INCREMENT, employee_name VARCHAR(100) NOT NULL, email VARCHAR(100) NOT NULL UNIQUE, phone VARCHAR(20) NOT NULL, department VARCHAR(50) NOT NULL, designation VARCHAR(50) NOT NULL, salary DECIMAL(10,2) NOT NULL, joining_date DATE NOT NULL, CONSTRAINT salary_nonnegative CHECK (salary >= 0))");
        }
    }

    public void ensureAdmin(String username, String password) throws SQLException {
        try (Connection c = connect(); PreparedStatement check = c.prepareStatement("SELECT COUNT(*) FROM admin")) {
            try (ResultSet result = check.executeQuery()) { result.next(); if (result.getInt(1) > 0) return; }
            if (username == null || username.isBlank() || username.length() > 50 || password == null || password.length() < 12 || password.length() > 128)
                throw new IllegalArgumentException("For first startup, set ADMIN_USERNAME (1–50 characters) and ADMIN_PASSWORD (12–128 characters).");
            try (PreparedStatement insert = c.prepareStatement("INSERT INTO admin (username,password_hash) VALUES (?,?)")) {
                insert.setString(1, username.trim()); insert.setString(2, Passwords.hash(password)); insert.executeUpdate();
            }
        }
    }

    public boolean authenticate(String username, String password) throws SQLException {
        try (Connection c = connect(); PreparedStatement s = c.prepareStatement("SELECT password_hash FROM admin WHERE username=?")) {
            s.setString(1, username);
            try (ResultSet r = s.executeQuery()) {
                // Hash even for unknown accounts to avoid a fast username-existence check.
                String hash = r.next() ? r.getString(1) : "600000:AAAAAAAAAAAAAAAAAAAAAA==:AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";
                return Passwords.verify(password, hash);
            }
        }
    }
}
