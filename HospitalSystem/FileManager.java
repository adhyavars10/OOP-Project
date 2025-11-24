import java.io.*;
import java.util.*;

// handles reading and writing files
public class FileManager {

    // reads all users from file
    public static ArrayList<User> readUsers() {
        System.out.println("[FileManager] reading users...");
        ArrayList<User> list = new ArrayList<>();
        BufferedReader br = null;
        File f = new File("users.txt");
        if (!f.exists()) {
            System.out.println("[FileManager] users.txt not found!");
            return list;
        }
        System.out.println("[FileManager] file path=" + f.getAbsolutePath());
        try {
            System.out.println("[FileManager] opening file");
            br = new BufferedReader(new FileReader("users.txt"));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println("[FileManager] line=" + line);
                if (line.trim().isEmpty()) continue;
                String[] arr = line.split(",");
                if (arr.length < 3) {
                    System.out.println("[FileManager] skipping bad line");
                    continue;
                }
                // format: username,password,role,name,specialization
                String u = arr[0];
                String p = arr[1];
                String r = arr[2];
                String name = arr.length > 3 ? arr[3] : "";
                if (r.equals("patient")) {
                    list.add(new Patient(u, p, name));
                } else if (r.equals("doctor")) {
                    String spec = arr.length > 4 ? arr[4] : "";
                    list.add(new Doctor(u, p, name, spec));
                } else {
                    list.add(new Receptionist(u, p, name));
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            try {
                if (br != null) br.close();
            } catch (Exception ignore) {
                System.out.println(
                    "[FileManager] error closing: " + ignore.getMessage()
                );
            }
            System.out.println(
                "[FileManager] done, loaded " + list.size() + " users"
            );
        }
        return list;
    }

    // reads appointments from file
    public static ArrayList<Appointment> readAppointments() {
        ArrayList<Appointment> list = new ArrayList<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader("appointments.txt"));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] arr = line.split(",");
                if (arr.length < 6) continue;
                int id;
                try {
                    id = Integer.parseInt(arr[0]);
                } catch (NumberFormatException n) {
                    continue;
                }
                String pat = arr[1];
                String doc = arr[2];
                String date = arr[3];
                String time = arr[4];
                String status = arr[5];
                list.add(new Appointment(id, pat, doc, date, time, status));
            }
        } catch (Exception e) {
            System.out.println("Error reading appointments");
        } finally {
            try {
                if (br != null) br.close();
            } catch (Exception ignore) {}
        }
        return list;
    }

    // saves new appointment to file
    public static void saveAppointment(Appointment a) {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter("appointments.txt", true));
            bw.write(
                a.getId() +
                    "," +
                    a.getPatientId() +
                    "," +
                    a.getDoctorId() +
                    "," +
                    a.getDate() +
                    "," +
                    a.getTime() +
                    "," +
                    a.getStatus()
            );
            bw.newLine();
        } catch (Exception e) {
            System.out.println("Error saving");
        } finally {
            try {
                if (bw != null) bw.close();
            } catch (Exception ignore) {}
        }
    }

    // updates appointment status
    public static boolean updateAppointment(int id, String newStatus) {
        ArrayList<Appointment> list = readAppointments();
        boolean found = false;
        for (int i = 0; i < list.size(); i++) {
            Appointment a = list.get(i);
            if (a.getId() == id) {
                a.setStatus(newStatus);
                found = true;
                break;
            }
        }
        if (!found) return false;
        // rewrite the whole file
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter("appointments.txt", false));
            for (int i = 0; i < list.size(); i++) {
                Appointment a = list.get(i);
                bw.write(
                    a.getId() +
                        "," +
                        a.getPatientId() +
                        "," +
                        a.getDoctorId() +
                        "," +
                        a.getDate() +
                        "," +
                        a.getTime() +
                        "," +
                        a.getStatus()
                );
                bw.newLine();
            }
        } catch (Exception e) {
            System.out.println("Error updating");
            return false;
        } finally {
            try {
                if (bw != null) bw.close();
            } catch (Exception ignore) {}
        }
        return true;
    }

    // gets next available id
    public static int nextAppointmentId() {
        int max = 0;
        ArrayList<Appointment> list = readAppointments();
        for (int i = 0; i < list.size(); i++) {
            int x = list.get(i).getId();
            if (x > max) max = x;
        }
        return max + 1;
    }

    // gets appointments for a patient
    public static ArrayList<Appointment> getForPatient(String pat) {
        ArrayList<Appointment> out = new ArrayList<>();
        ArrayList<Appointment> all = readAppointments();
        for (int i = 0; i < all.size(); i++) {
            Appointment a = all.get(i);
            if (a.getPatientId().equals(pat)) out.add(a);
        }
        return out;
    }

    // gets appointments for a doctor
    public static ArrayList<Appointment> getForDoctor(String doc) {
        ArrayList<Appointment> out = new ArrayList<>();
        ArrayList<Appointment> all = readAppointments();
        for (int i = 0; i < all.size(); i++) {
            Appointment a = all.get(i);
            if (a.getDoctorId().equals(doc)) out.add(a);
        }
        return out;
    }
}
