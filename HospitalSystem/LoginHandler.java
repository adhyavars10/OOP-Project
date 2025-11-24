import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

// handles login requests
public class LoginHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange ex) throws IOException {
        // only accept POST
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            write(ex, 405, "{\"success\":false,\"error\":\"POST only\"}");
            return;
        }

        String body = readBody(ex.getRequestBody());
        System.out.println("[LOGIN] got body=" + body);

        String username = "";
        String password = "";

        // parse the form data
        String[] parts = body.split("&");
        for (int i = 0; i < parts.length; i++) {
            String p = parts[i];
            String[] kv = p.split("=", 2);
            if (kv.length == 2) {
                String key = decode(kv[0]);
                String val = decode(kv[1]);
                if (key.equals("username")) {
                    username = val;
                } else if (key.equals("password")) {
                    password = val;
                }
            }
        }

        // check if we got both username and password
        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("[LOGIN] missing username or password");
            write(ex, 200, "{\"success\":false,\"error\":\"missing\"}");
            return;
        }

        // load users from file
        ArrayList<User> users = FileManager.readUsers();
        User found = null;
        System.out.println("[LOGIN] checking " + users.size() + " users");

        // check each user
        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            System.out.println("[LOGIN] checking user: " + u.getUsername());
            if (
                u.getUsername().equals(username) &&
                u.getPassword().equals(password)
            ) {
                found = u;
                System.out.println("[LOGIN] found match! role=" + u.getRole());
                break;
            }
        }

        // if no user found
        if (found == null) {
            System.out.println("[LOGIN] login failed for: " + username);
            write(ex, 200, "{\"success\":false}");
            return;
        }

        // login success!
        String role = found.getRole();
        String name = found.getName();
        String json =
            "{\"success\":true,\"role\":\"" +
            escape(role) +
            "\",\"name\":\"" +
            escape(name) +
            "\"}";
        System.out.println(
            "[LOGIN] success! user=" + username + " role=" + role
        );
        write(ex, 200, json);
    }

    // reads the request body
    private String readBody(InputStream in) {
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int r;
            while ((r = in.read(buf)) != -1) {
                bout.write(buf, 0, r);
            }
            return bout.toString(StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }

    // decodes URL encoded strings
    private String decode(String s) {
        try {
            return URLDecoder.decode(s, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return s;
        }
    }

    // escapes quotes in strings for JSON
    private String escape(String s) {
        if (s == null) return "";
        String result = s.replace("\\", "\\\\");
        result = result.replace("\"", "\\\"");
        return result;
    }

    // writes response
    private void write(HttpExchange ex, int code, String data)
        throws IOException {
        byte[] b = data.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json");
        ex.sendResponseHeaders(code, b.length);
        OutputStream os = ex.getResponseBody();
        os.write(b);
        os.close();
    }
}
