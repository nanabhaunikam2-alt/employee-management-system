package com.bhagyashree.peopledesk;

import com.google.gson.JsonParser;
import org.apache.catalina.startup.Tomcat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.*;
import java.nio.file.Path;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class ApiTest {
    @TempDir Path temporary;
    @Test void sessionProtectionAndEmployeeLifecycle() throws Exception {
        Database db = new Database("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        db.initialize(); db.ensureAdmin("admin", "a-long-test-password");
        Tomcat tomcat = new Tomcat(); tomcat.setBaseDir(temporary.resolve("tomcat").toString()); tomcat.setPort(0);
        tomcat.getConnector().setProperty("address", "127.0.0.1");
        var context = tomcat.addContext("", temporary.toString());
        Tomcat.addServlet(context,"api",new ApiServlet(db)); context.addServletMappingDecoded("/api/*","api");
        try {
            tomcat.start();
            String base = "http://127.0.0.1:" + tomcat.getConnector().getLocalPort() + "/api";
            HttpClient client = HttpClient.newBuilder().cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL)).build();
            assertEquals(401, send(client,base,"/employees","GET",null,null).statusCode());
            assertEquals(401, send(client,base,"/login","POST","{\"username\":\"admin\",\"password\":\"wrong\"}",null).statusCode());
            var login = send(client,base,"/login","POST","{\"username\":\"admin\",\"password\":\"a-long-test-password\"}",null);
            assertEquals(200, login.statusCode());
            String csrf = JsonParser.parseString(login.body()).getAsJsonObject().get("csrfToken").getAsString();
            assertEquals(200, send(client,base,"/session","GET",null,null).statusCode());
            String body = "{\"name\":\"API Person\",\"email\":\"api@example.com\",\"phone\":\"9876543210\",\"department\":\"Engineering\",\"designation\":\"Developer\",\"salary\":45000,\"joiningDate\":\"2026-10-29\"}";
            assertEquals(403, send(client,base,"/employees","POST",body,null).statusCode());
            assertEquals(400, send(client,base,"/employees","POST","{\"name\":\"invalid\"}",csrf).statusCode());
            var created = send(client,base,"/employees","POST",body,csrf); assertEquals(201,created.statusCode(),created.body());
            long id = JsonParser.parseString(created.body()).getAsJsonObject().get("id").getAsLong();
            assertEquals(409, send(client,base,"/employees","POST",body,csrf).statusCode());
            assertEquals(1,JsonParser.parseString(send(client,base,"/employees","GET",null,null).body()).getAsJsonArray().size());
            assertEquals(200,send(client,base,"/employees/"+id,"PUT",body.replace("API Person","Updated Person"),csrf).statusCode());
            assertEquals(200,send(client,base,"/employees/"+id,"DELETE",null,csrf).statusCode());
            assertEquals(404,send(client,base,"/employees/"+id,"DELETE",null,csrf).statusCode());
            assertEquals(200,send(client,base,"/logout","POST",null,csrf).statusCode());
            assertEquals(401,send(client,base,"/employees","GET",null,null).statusCode());
        } finally { tomcat.stop(); tomcat.destroy(); }
    }
    private HttpResponse<String> send(HttpClient client, String base, String path, String method, String body, String csrf) throws Exception {
        var builder = HttpRequest.newBuilder(URI.create(base+path)).header("Content-Type","application/json");
        if (csrf != null) builder.header("X-CSRF-Token",csrf);
        builder.method(method,body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        return client.send(builder.build(),HttpResponse.BodyHandlers.ofString());
    }
}
