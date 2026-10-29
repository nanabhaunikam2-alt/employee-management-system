package com.bhagyashree.peopledesk;

import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.http.Rfc6265CookieProcessor;

public class Application {
    public static void main(String[] args) throws Exception {
        // Windows socket paths can exceed their length limit under a long user TEMP path.
        Path socketDirectory = Path.of(".runtime").toAbsolutePath();
        Files.createDirectories(socketDirectory);
        System.setProperty("jdk.net.unixdomain.tmpdir",socketDirectory.toString());
        String url = System.getenv().getOrDefault("DB_URL","jdbc:mysql://localhost:3306/employee_management?connectionTimeZone=Asia/Kolkata");
        Database db = new Database(url,System.getenv().getOrDefault("DB_USER","peopledesk"),System.getenv().getOrDefault("DB_PASSWORD",""));
        db.initialize(); db.ensureAdmin(System.getenv("ADMIN_USERNAME"),System.getenv("ADMIN_PASSWORD"));
        Path frontend = Path.of(System.getenv().getOrDefault("FRONTEND_DIR","../frontend")).toAbsolutePath().normalize();
        if (!Files.isRegularFile(frontend.resolve("index.html"))) throw new IllegalStateException("Frontend not found. Run from the backend directory.");
        Tomcat tomcat = new Tomcat();
        tomcat.setBaseDir(".runtime"); tomcat.setPort(Integer.parseInt(System.getenv().getOrDefault("PORT","8080")));
        tomcat.getConnector().setProperty("address",System.getenv().getOrDefault("BIND_ADDRESS","127.0.0.1")); tomcat.getConnector().setProperty("maxPostSize","16384"); tomcat.getConnector().setProperty("server","PeopleDesk");
        // Phone mode keeps the laptop's localhost URL working as well as the Wi-Fi URL.
        if (!System.getenv().getOrDefault("BIND_ADDRESS","127.0.0.1").equals("127.0.0.1")) {
            org.apache.catalina.connector.Connector local = new org.apache.catalina.connector.Connector("org.apache.coyote.http11.Http11NioProtocol");
            local.setPort(tomcat.getConnector().getPort());
            local.setProperty("address","127.0.0.1"); local.setProperty("maxPostSize","16384"); local.setProperty("server","PeopleDesk");
            tomcat.getService().addConnector(local);
        }
        Context context = tomcat.addContext("",frontend.toString());
        context.setSessionCookieName("PEOPLEDESK_SESSION"); context.setUseHttpOnly(true);
        context.addServletContainerInitializer((classes, servletContext) ->
            servletContext.setSessionTrackingModes(java.util.Set.of(jakarta.servlet.SessionTrackingMode.COOKIE)), java.util.Set.of());
        Rfc6265CookieProcessor cookies = new Rfc6265CookieProcessor(); cookies.setSameSiteCookies("strict"); context.setCookieProcessor(cookies);
        Tomcat.addServlet(context,"api",new ApiServlet(db)); context.addServletMappingDecoded("/api/*","api");
        Tomcat.addServlet(context,"assets",new org.apache.catalina.servlets.DefaultServlet()); context.addServletMappingDecoded("/","assets"); context.addWelcomeFile("index.html");
        tomcat.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> { try { tomcat.stop(); tomcat.destroy(); } catch (Exception ignored) { } }));
        System.out.println("PeopleDesk: http://" + System.getenv().getOrDefault("BIND_ADDRESS","127.0.0.1") + ":" + tomcat.getConnector().getPort());
        tomcat.getServer().await();
    }
}
