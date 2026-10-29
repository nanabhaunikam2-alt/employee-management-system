package com.bhagyashree.peopledesk;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class Employee {
    public long id;
    public String name, email, phone, department, designation, joiningDate;
    public BigDecimal salary;

    public void validate() {
        name = required(name, 100, "Name");
        email = required(email, 100, "Email").toLowerCase(Locale.ROOT);
        phone = required(phone, 20, "Phone");
        department = required(department, 50, "Department");
        designation = required(designation, 50, "Designation");
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) throw new IllegalArgumentException("Enter a valid email address.");
        if (!phone.matches("[+0-9() -]{7,20}")) throw new IllegalArgumentException("Enter a valid phone number.");
        if (salary == null || salary.signum() < 0 || salary.compareTo(new BigDecimal("99999999.99")) > 0 || salary.stripTrailingZeros().scale() > 2)
            throw new IllegalArgumentException("Salary must be between 0 and 99999999.99, with at most two decimal places.");
        try {
            LocalDate date = LocalDate.parse(joiningDate == null ? "" : joiningDate);
            if (date.getYear() < 1000 || date.getYear() > 9999) throw new IllegalArgumentException("Joining date must be between years 1000 and 9999.");
        } catch (DateTimeParseException e) { throw new IllegalArgumentException("Enter a valid joining date."); }
    }

    private static String required(String value, int limit, String field) {
        if (value == null || value.isBlank() || value.trim().length() > limit) throw new IllegalArgumentException(field + " is required and must be at most " + limit + " characters.");
        return value.trim();
    }
}
