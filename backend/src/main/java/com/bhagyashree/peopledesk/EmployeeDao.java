package com.bhagyashree.peopledesk;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EmployeeDao {
    private final Database database;
    public EmployeeDao(Database database) { this.database = database; }

    public List<Employee> findAll() throws SQLException {
        List<Employee> employees = new ArrayList<>();
        try (Connection c = database.connect(); PreparedStatement s = c.prepareStatement("SELECT * FROM employee ORDER BY employee_id"); ResultSet r = s.executeQuery()) {
            while (r.next()) {
                Employee e = new Employee(); e.id = r.getLong("employee_id"); e.name = r.getString("employee_name"); e.email = r.getString("email"); e.phone = r.getString("phone"); e.department = r.getString("department"); e.designation = r.getString("designation"); e.salary = r.getBigDecimal("salary"); e.joiningDate = r.getDate("joining_date").toLocalDate().toString(); employees.add(e);
            }
        }
        return employees;
    }

    public Employee save(Employee e, Long id) throws SQLException {
        e.validate();
        String sql = id == null ? "INSERT INTO employee (employee_name,email,phone,department,designation,salary,joining_date) VALUES (?,?,?,?,?,?,?)" : "UPDATE employee SET employee_name=?,email=?,phone=?,department=?,designation=?,salary=?,joining_date=? WHERE employee_id=?";
        try (Connection c = database.connect(); PreparedStatement s = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            s.setString(1,e.name); s.setString(2,e.email); s.setString(3,e.phone); s.setString(4,e.department); s.setString(5,e.designation); s.setBigDecimal(6,e.salary); s.setDate(7, Date.valueOf(e.joiningDate));
            if (id != null) s.setLong(8,id);
            if (s.executeUpdate() == 0) return null;
            if (id == null) { try (ResultSet keys = s.getGeneratedKeys()) { keys.next(); e.id = keys.getLong(1); } } else e.id = id;
        }
        return e;
    }

    public boolean delete(long id) throws SQLException {
        try (Connection c = database.connect(); PreparedStatement s = c.prepareStatement("DELETE FROM employee WHERE employee_id=?")) { s.setLong(1,id); return s.executeUpdate() > 0; }
    }
}
