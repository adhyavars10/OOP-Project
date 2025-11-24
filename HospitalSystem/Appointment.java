// stores appointment info
public class Appointment {

    private int id;
    private String patientId;
    private String doctorId;
    private String date; // like 2024-01-15
    private String time; // like 10:30
    private String status; // PENDING, COMPLETED, or CANCELLED

    // constructor
    public Appointment(
        int i,
        String p,
        String d,
        String dt,
        String t,
        String s
    ) {
        this.id = i;
        this.patientId = p;
        this.doctorId = d;
        this.date = dt;
        this.time = t;
        this.status = s;
    }

    // getters
    public int getId() {
        return id;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getStatus() {
        return status;
    }

    // setters
    public void setId(int v) {
        id = v;
    }

    public void setPatientId(String v) {
        patientId = v;
    }

    public void setDoctorId(String v) {
        doctorId = v;
    }

    public void setDate(String v) {
        date = v;
    }

    public void setTime(String v) {
        time = v;
    }

    public void setStatus(String v) {
        status = v;
    }

    @Override
    public String toString() {
        return (
            id +
            "," +
            patientId +
            "," +
            doctorId +
            "," +
            date +
            "," +
            time +
            "," +
            status
        );
    }
}
