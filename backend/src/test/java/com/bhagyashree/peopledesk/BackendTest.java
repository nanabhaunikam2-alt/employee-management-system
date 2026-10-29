package com.bhagyashree.peopledesk;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class BackendTest {
    private Database database() throws SQLException {
        Database db = new Database("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        db.initialize(); return db;
    }
    private Employee employee() {
        Employee e = new Employee(); e.name = "Test Person"; e.email = "test@example.com"; e.phone = "9876543210"; e.department = "Engineering"; e.designation = "Developer"; e.salary = new BigDecimal("45000.25"); e.joiningDate = "2026-10-29"; return e;
    }
    @Test void passwordsAreSaltedAndVerified() {
        String first = Passwords.hash("a-long-test-password");
        assertNotEquals(first, Passwords.hash("a-long-test-password"));
        assertTrue(Passwords.verify("a-long-test-password", first));
        assertFalse(Passwords.verify("wrong", first));
        assertFalse(Passwords.verify("anything", "invalid"));
    }
    @Test void adminIsCreatedOnceAndAuthenticates() throws Exception {
        Database db = database(); db.ensureAdmin("admin", "a-long-test-password");
        db.ensureAdmin("another", "another-test-password");
        assertTrue(db.authenticate("admin", "a-long-test-password"));
        assertFalse(db.authenticate("admin", "wrong"));
        assertFalse(db.authenticate("another", "another-test-password"));
        assertFalse(db.authenticate("' OR 1=1 --", "wrong"));
    }
    @Test void databaseRoundTripAndDuplicateEmail() throws Exception {
        EmployeeDao dao = new EmployeeDao(database());
        Employee e = dao.save(employee(), null);
        assertTrue(e.id > 0); assertEquals(1, dao.findAll().size());
        assertEquals(new BigDecimal("45000.25"), dao.findAll().get(0).salary);
        assertThrows(SQLException.class, () -> dao.save(employee(), null));
        e.name = "Updated Name"; dao.save(e, e.id);
        assertEquals("Updated Name", dao.findAll().get(0).name);
        assertNull(dao.save(e, 99999L));
        assertTrue(dao.delete(e.id)); assertFalse(dao.delete(e.id)); assertTrue(dao.findAll().isEmpty());
    }
    @Test void rejectsInvalidInputs() {
        Employee e = employee(); e.salary = new BigDecimal("-1"); assertThrows(IllegalArgumentException.class,e::validate);
        e.salary = new BigDecimal("1.001"); assertThrows(IllegalArgumentException.class,e::validate);
        e.salary = BigDecimal.ZERO; e.joiningDate = "2026-02-30"; assertThrows(IllegalArgumentException.class,e::validate);
        e.joiningDate = "2026-10-29"; e.name = "   "; assertThrows(IllegalArgumentException.class,e::validate);
    }
}
