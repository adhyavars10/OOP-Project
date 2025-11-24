public class Doctor extends User implements calledAboratory {

    private String specialization;

    // constructor for doctor
    public Doctor(String u, String pwd, String nm, String spec) {
        super(u, pwd, "doctor", nm);
        this.specialization = spec;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String s) {
        this.specialization = s;
    }

    // just prints the doctor info
    public void displayInfo() {
        System.out.println("Doctor " + getName() + " (" + specialization + ")");
    }

    @Override
    public String toString() {
        return (
            "Doctor{" +
            getUsername() +
            "," +
            getName() +
            "," +
            specialization +
            "}"
        );
    }
}
