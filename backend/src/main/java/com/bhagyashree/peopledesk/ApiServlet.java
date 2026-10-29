package com.bhagyashree.peopledesk;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Map;

public class ApiServlet extends HttpServlet {
    private final Database database;
    private final EmployeeDao employees;
    private final Gson json = new Gson();
    // The application binds only to localhost. This shared limit bounds demo login guessing.
    private int failures;
    private long retryAfter;
    public ApiServlet(Database database) { this.database = database; employees = new EmployeeDao(database); }

    @Override protected void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json"); response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store"); response.setHeader("X-Content-Type-Options", "nosniff");
        String path = request.getPathInfo() == null ? "/" : request.getPathInfo();
        String method = request.getMethod();
        try {
            if (path.equals("/login") && method.equals("POST")) { login(request,response); return; }
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("username") == null) { send(response,401,Map.of("error","Please sign in to continue.")); return; }
            if (path.equals("/session") && method.equals("GET")) { send(response,200,Map.of("username",session.getAttribute("username"),"csrfToken",session.getAttribute("csrf"))); return; }
            if (!method.equals("GET") && !String.valueOf(session.getAttribute("csrf")).equals(request.getHeader("X-CSRF-Token"))) { send(response,403,Map.of("error","Session verification failed. Sign in again.")); return; }
            if (path.equals("/logout") && method.equals("POST")) { session.invalidate(); send(response,200,Map.of("ok",true)); return; }
            if (path.equals("/employees")) {
                if (method.equals("GET")) { send(response,200,employees.findAll()); return; }
                if (method.equals("POST")) { send(response,201,employees.save(body(request,Employee.class),null)); return; }
            }
            if (path.matches("/employees/[1-9][0-9]{0,17}")) {
                long id = Long.parseLong(path.substring(path.lastIndexOf('/')+1));
                if (method.equals("PUT")) { Employee saved = employees.save(body(request,Employee.class),id); send(response,saved == null ? 404 : 200,saved == null ? Map.of("error","Employee not found.") : saved); return; }
                if (method.equals("DELETE")) { boolean deleted = employees.delete(id); send(response,deleted ? 200 : 404,deleted ? Map.of("ok",true) : Map.of("error","Employee not found.")); return; }
            }
            send(response,404,Map.of("error","Endpoint not found."));
        } catch (IllegalArgumentException | JsonParseException e) { send(response,400,Map.of("error",e instanceof JsonParseException ? "Enter valid JSON details." : e.getMessage())); }
        catch (SQLException e) {
            if (e.getErrorCode() == 1062 || "23505".equals(e.getSQLState())) send(response,409,Map.of("error","An employee with this email address already exists."));
            else { getServletContext().log("Database request failed",e); send(response,503,Map.of("error","Database unavailable. Check the server and try again.")); }
        }
    }

    private synchronized void login(HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
        long now = System.currentTimeMillis();
        if (now < retryAfter) { response.setHeader("Retry-After","60"); send(response,429,Map.of("error","Too many login attempts. Try again in a minute.")); return; }
        Credentials credentials = body(request,Credentials.class);
        if (credentials.username == null || credentials.password == null || credentials.username.length() > 50 || credentials.password.length() > 128) throw new IllegalArgumentException("Enter a valid username and password.");
        if (!database.authenticate(credentials.username.trim(),credentials.password)) {
            if (++failures >= 5) { retryAfter = now + 60_000; failures = 0; }
            send(response,401,Map.of("error","Incorrect username or password.")); return;
        }
        failures = 0; retryAfter = 0;
        HttpSession old = request.getSession(false); if (old != null) old.invalidate();
        HttpSession session = request.getSession(true); session.setMaxInactiveInterval(1800);
        byte[] token = new byte[32]; new SecureRandom().nextBytes(token);
        String csrf = Base64.getUrlEncoder().withoutPadding().encodeToString(token);
        session.setAttribute("username",credentials.username.trim()); session.setAttribute("csrf",csrf);
        send(response,200,Map.of("username",credentials.username.trim(),"csrfToken",csrf));
    }

    private <T> T body(HttpServletRequest request, Class<T> type) throws IOException {
        if (request.getContentType() == null || !request.getContentType().toLowerCase().startsWith("application/json")) throw new IllegalArgumentException("Send JSON data.");
        char[] buffer = new char[16385]; int offset = 0, read;
        var reader = request.getReader();
        while (offset < buffer.length && (read = reader.read(buffer,offset,buffer.length-offset)) != -1) offset += read;
        if (offset > 16384) throw new IllegalArgumentException("Request is too large.");
        T value = json.fromJson(new String(buffer,0,offset),type);
        if (value == null) throw new IllegalArgumentException("Request data is required.");
        return value;
    }
    private void send(HttpServletResponse response, int status, Object value) throws IOException { response.setStatus(status); json.toJson(value,response.getWriter()); }
    private static class Credentials { String username, password; }
}
