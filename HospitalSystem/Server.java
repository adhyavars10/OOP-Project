import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

// simple HTTP server
public class Server {

    public static void main(String[] args) throws Exception {
        // create server on port 8080
        HttpServer srv = HttpServer.create(new InetSocketAddress(8080), 0);

        // setup routes
        srv.createContext("/", new StaticHandler());
        srv.createContext("/api/login", new LoginHandler());
        srv.createContext("/api/appointments", new AppointmentsHandler());
        srv.createContext("/api/book", new BookHandler());
        srv.createContext("/api/cancel", new CancelHandler());

        srv.setExecutor(null);
        System.out.println("Server started on port 8080");
        srv.start();
    }

    // serves HTML and CSS files
    static class StaticHandler implements HttpHandler {

        public void handle(HttpExchange ex) throws IOException {
            String path = ex.getRequestURI().getPath();

            // default to index
            if (path.equals("/") || path.equals("/index.html")) {
                path = "index.html";
            } else {
                path = path.substring(1);
            }

            // check if valid file
            if (
                !path.startsWith("web/") &&
                !path.startsWith("index") &&
                !path.startsWith("patient") &&
                !path.startsWith("doctor") &&
                !path.startsWith("receptionist") &&
                !path.startsWith("style.css")
            ) {
                path = "web/index.html";
            }

            if (!path.startsWith("web/")) {
                path = "web/" + path;
            }

            File f = new File(path);
            if (!f.exists() || f.isDirectory()) {
                writeBytes(
                    ex,
                    404,
                    "Not Found".getBytes(StandardCharsets.UTF_8),
                    "text/plain"
                );
                return;
            }

            // read file
            byte[] data = readFileBytes(f);

            // figure out content type
            String type = "text/plain";
            if (path.endsWith(".html")) {
                type = "text/html";
            } else if (path.endsWith(".css")) {
                type = "text/css";
            } else if (path.endsWith(".js")) {
                type = "application/javascript";
            }

            writeBytes(ex, 200, data, type);
        }
    }

    // handles GET /api/appointments
    static class AppointmentsHandler implements HttpHandler {

        public void handle(HttpExchange ex) throws IOException {
            if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
                write(ex, 405, "{\"error\":\"GET only\"}");
                return;
            }

            String q = ex.getRequestURI().getQuery();
            String user = "";
            String role = "";

            // parse query params
            if (q != null) {
                String[] arr = q.split("&");
                for (int i = 0; i < arr.length; i++) {
                    String[] kv = arr[i].split("=", 2);
                    if (kv.length == 2) {
                        String k = decode(kv[0]);
                        String v = decode(kv[1]);
                        if (k.equals("user")) {
                            user = v;
                        } else if (k.equals("role")) {
                            role = v;
                        }
                    }
                }
            }

            // get appointments based on role
            ArrayList<Appointment> list;
            if (role.equals("patient")) {
                list = FileManager.getForPatient(user);
            } else if (role.equals("doctor")) {
                list = FileManager.getForDoctor(user);
            } else if (role.equals("receptionist")) {
                list = FileManager.readAppointments();
            } else {
                list = new ArrayList<>();
            }

            // build JSON manually
            String json = "{\"list\":[";
            for (int i = 0; i < list.size(); i++) {
                Appointment a = list.get(i);
                json = json + "{\"id\":" + a.getId();
                json =
                    json + ",\"patientId\":\"" + esc(a.getPatientId()) + "\"";
                json = json + ",\"doctorId\":\"" + esc(a.getDoctorId()) + "\"";
                json = json + ",\"date\":\"" + esc(a.getDate()) + "\"";
                json = json + ",\"time\":\"" + esc(a.getTime()) + "\"";
                json = json + ",\"status\":\"" + esc(a.getStatus()) + "\"}";
                if (i < list.size() - 1) {
                    json = json + ",";
                }
            }
            json = json + "]}";
            write(ex, 200, json);
        }
    }

    // handles POST /api/book
    static class BookHandler implements HttpHandler {

        public void handle(HttpExchange ex) throws IOException {
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
                write(ex, 405, "{\"success\":false,\"error\":\"POST only\"}");
                return;
            }

            String body = readBody(ex.getRequestBody());
            String patientId = "";
            String doctorId = "";
            String date = "";
            String time = "";

            // parse form data
            String[] parts = body.split("&");
            for (int i = 0; i < parts.length; i++) {
                String[] kv = parts[i].split("=", 2);
                if (kv.length == 2) {
                    String k = decode(kv[0]);
                    String v = decode(kv[1]);
                    if (k.equals("patientId")) {
                        patientId = v;
                    } else if (k.equals("doctorId")) {
                        doctorId = v;
                    } else if (k.equals("date")) {
                        date = v;
                    } else if (k.equals("time")) {
                        time = v;
                    }
                }
            }

            // check if we got all fields
            if (
                patientId.isEmpty() ||
                doctorId.isEmpty() ||
                date.isEmpty() ||
                time.isEmpty()
            ) {
                write(ex, 200, "{\"success\":false,\"error\":\"missing\"}");
                return;
            }

            // check if slot is taken
            ArrayList<Appointment> all = FileManager.readAppointments();
            for (int i = 0; i < all.size(); i++) {
                Appointment a = all.get(i);
                if (
                    a.getDoctorId().equals(doctorId) &&
                    a.getDate().equals(date) &&
                    a.getTime().equals(time) &&
                    !a.getStatus().equalsIgnoreCase("CANCELLED")
                ) {
                    write(
                        ex,
                        200,
                        "{\"success\":false,\"error\":\"slot taken\"}"
                    );
                    return;
                }
            }

            // create new appointment
            int id = FileManager.nextAppointmentId();
            Appointment newA = new Appointment(
                id,
                patientId,
                doctorId,
                date,
                time,
                "PENDING"
            );
            FileManager.saveAppointment(newA);
            write(ex, 200, "{\"success\":true,\"id\":" + id + "}");
        }
    }

    // handles POST /api/cancel
    static class CancelHandler implements HttpHandler {

        public void handle(HttpExchange ex) throws IOException {
            if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
                write(ex, 405, "{\"success\":false}");
                return;
            }

            String body = readBody(ex.getRequestBody());
            String idStr = "";
            String doneFlag = "";

            // parse form data
            String[] arr = body.split("&");
            for (int i = 0; i < arr.length; i++) {
                String[] kv = arr[i].split("=", 2);
                if (kv.length == 2) {
                    String k = decode(kv[0]);
                    String v = decode(kv[1]);
                    if (k.equals("appointmentId")) {
                        idStr = v;
                    } else if (k.equals("done")) {
                        doneFlag = v;
                    }
                }
            }

            // parse id
            int id = -1;
            try {
                id = Integer.parseInt(idStr);
            } catch (Exception e) {
                // ignore
            }

            if (id < 0) {
                write(ex, 200, "{\"success\":false,\"error\":\"bad id\"}");
                return;
            }

            // update status
            String newStatus = "CANCELLED";
            if (doneFlag.equals("1")) {
                newStatus = "COMPLETED";
            }

            boolean ok = FileManager.updateAppointment(id, newStatus);
            if (ok) {
                write(ex, 200, "{\"success\":true}");
            } else {
                write(ex, 200, "{\"success\":false,\"error\":\"not found\"}");
            }
        }
    }

    // helper - reads request body
    static String readBody(InputStream in) {
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

    // helper - decodes URL strings
    static String decode(String s) {
        try {
            return URLDecoder.decode(s, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return s;
        }
    }

    // helper - escapes quotes for JSON
    static String esc(String s) {
        if (s == null) return "";
        String result = s.replace("\\", "\\\\");
        result = result.replace("\"", "\\\"");
        return result;
    }

    // helper - writes JSON response
    static void write(HttpExchange ex, int code, String data)
        throws IOException {
        byte[] b = data.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json");
        ex.sendResponseHeaders(code, b.length);
        OutputStream os = ex.getResponseBody();
        os.write(b);
        os.close();
    }

    // helper - writes byte response
    static void writeBytes(HttpExchange ex, int code, byte[] data, String type)
        throws IOException {
        ex.getResponseHeaders().set("Content-Type", type);
        ex.sendResponseHeaders(code, data.length);
        OutputStream os = ex.getResponseBody();
        os.write(data);
        os.close();
    }

    // helper - reads file into bytes
    static byte[] readFileBytes(File f) {
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            FileInputStream fin = new FileInputStream(f);
            byte[] buf = new byte[4096];
            int r;
            while ((r = fin.read(buf)) != -1) {
                bout.write(buf, 0, r);
            }
            fin.close();
            return bout.toByteArray();
        } catch (Exception e) {
            return "error".getBytes(StandardCharsets.UTF_8);
        }
    }
}
