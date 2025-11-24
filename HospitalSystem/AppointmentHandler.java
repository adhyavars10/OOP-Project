import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

// handles appointment API stuff
public class AppointmentHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        if (path == null) path = "";

        // figure out which endpoint they want
        if (path.endsWith("/api/appointments")) {
            handleList(ex);
        } else if (path.endsWith("/api/book")) {
            handleBook(ex);
        } else if (path.endsWith("/api/cancel")) {
            handleCancel(ex);
        } else {
            write(ex, 404, "{\"success\":false,\"error\":\"not found\"}");
        }
    }

    // list appointments
    private void handleList(HttpExchange ex) throws IOException {
        if (!"GET".equalsIgnoreCase(ex.getRequestMethod())) {
            write(ex, 405, "{\"success\":false,\"error\":\"GET only\"}");
            return;
        }

        String q = ex.getRequestURI().getRawQuery();
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
                    if (k.equals("user")) user = v;
                    else if (k.equals("role")) role = v;
                }
            }
        }

        // get the right appointments based on role
        ArrayList<Appointment> list;
        if (role.equals("patient")) {
            list = FileManager.getForPatient(user);
        } else if (role.equals("doctor")) {
            list = FileManager.getForDoctor(user);
        } else {
            // receptionist sees all
            list = FileManager.readAppointments();
        }

        // build JSON response
        String json = "{\"success\":true,\"list\":[";
        for (int i = 0; i < list.size(); i++) {
            Appointment a = list.get(i);
            if (i > 0) json = json + ",";
            json = json + "{\"id\":" + a.getId();
            json = json + ",\"patientId\":\"" + esc(a.getPatientId()) + "\"";
            json = json + ",\"doctorId\":\"" + esc(a.getDoctorId()) + "\"";
            json = json + ",\"date\":\"" + esc(a.getDate()) + "\"";
            json = json + ",\"time\":\"" + esc(a.getTime()) + "\"";
            json = json + ",\"status\":\"" + esc(a.getStatus()) + "\"}";
        }
        json = json + "]}";
        write(ex, 200, json);
    }

    // book new appointment
    private void handleBook(HttpExchange ex) throws IOException {
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
                if (k.equals("patientId")) patientId = v;
                else if (k.equals("doctorId")) doctorId = v;
                else if (k.equals("date")) date = v;
                else if (k.equals("time")) time = v;
            }
        }

        // make sure we got everything
        if (
            patientId.isEmpty() ||
            doctorId.isEmpty() ||
            date.isEmpty() ||
            time.isEmpty()
        ) {
            write(ex, 200, "{\"success\":false,\"error\":\"missing fields\"}");
            return;
        }

        // check date format
        if (date.length() != 10) {
            write(ex, 200, "{\"success\":false,\"error\":\"bad date\"}");
            return;
        }

        // check if slot is already taken
        ArrayList<Appointment> all = FileManager.readAppointments();
        for (int i = 0; i < all.size(); i++) {
            Appointment a = all.get(i);
            if (
                a.getDoctorId().equals(doctorId) &&
                a.getDate().equals(date) &&
                a.getTime().equals(time) &&
                !a.getStatus().equalsIgnoreCase("CANCELLED")
            ) {
                write(ex, 200, "{\"success\":false,\"error\":\"slot taken\"}");
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

    // cancel or mark done
    private void handleCancel(HttpExchange ex) throws IOException {
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            write(ex, 405, "{\"success\":false,\"error\":\"POST only\"}");
            return;
        }

        String body = readBody(ex.getRequestBody());
        String apptId = "";
        boolean doneFlag = false;

        // parse form data
        String[] parts = body.split("&");
        for (int i = 0; i < parts.length; i++) {
            String[] kv = parts[i].split("=", 2);
            if (kv.length == 2) {
                String k = decode(kv[0]);
                String v = decode(kv[1]);
                if (k.equals("appointmentId")) apptId = v;
                else if (k.equals("done") && v.equals("1")) doneFlag = true;
            }
        }

        // convert id to int
        int id = -1;
        try {
            id = Integer.parseInt(apptId);
        } catch (NumberFormatException e) {
            // bad id
        }

        if (id < 0) {
            write(ex, 200, "{\"success\":false,\"error\":\"bad id\"}");
            return;
        }

        // find the appointment
        ArrayList<Appointment> all = FileManager.readAppointments();
        Appointment target = null;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).getId() == id) {
                target = all.get(i);
                break;
            }
        }

        if (target == null) {
            write(ex, 200, "{\"success\":false,\"error\":\"not found\"}");
            return;
        }

        // update status
        if (doneFlag) {
            // mark as completed
            if (!target.getStatus().equalsIgnoreCase("PENDING")) {
                write(ex, 200, "{\"success\":false,\"error\":\"not pending\"}");
                return;
            }
            boolean ok = FileManager.updateAppointment(id, "COMPLETED");
            if (ok) {
                write(ex, 200, "{\"success\":true}");
            } else {
                write(ex, 200, "{\"success\":false,\"error\":\"update fail\"}");
            }
        } else {
            // cancel it
            if (!target.getStatus().equalsIgnoreCase("PENDING")) {
                write(
                    ex,
                    200,
                    "{\"success\":false,\"error\":\"only pending\"}"
                );
                return;
            }
            boolean ok = FileManager.updateAppointment(id, "CANCELLED");
            if (ok) {
                write(ex, 200, "{\"success\":true}");
            } else {
                write(ex, 200, "{\"success\":false,\"error\":\"update fail\"}");
            }
        }
    }

    // helper to read request body
    private String readBody(InputStream in) {
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            byte[] buf = new byte[512];
            int r;
            while ((r = in.read(buf)) != -1) {
                bout.write(buf, 0, r);
            }
            return bout.toString(StandardCharsets.UTF_8);
        } catch (Exception e) {
            return "";
        }
    }

    // helper to decode URL stuff
    private String decode(String s) {
        try {
            return URLDecoder.decode(s, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            return s;
        }
    }

    // helper to write response
    private void write(HttpExchange ex, int code, String data)
        throws IOException {
        byte[] b = data.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json");
        ex.sendResponseHeaders(code, b.length);
        OutputStream os = ex.getResponseBody();
        os.write(b);
        os.close();
    }

    // helper to escape quotes for JSON
    private String esc(String s) {
        if (s == null) return "";
        String result = s.replace("\\", "\\\\");
        result = result.replace("\"", "\\\"");
        return result;
    }
}
