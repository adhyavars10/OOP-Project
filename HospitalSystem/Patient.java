public class Patient extends User implements calledAboratory {

    // constructor
    public Patient(String u, String pwd, String nm) {
        super(u, pwd, "patient", nm);
    }

    // extra field for notes or something
    private String note;

    public String getNote() {
        return note;
    }

    public void setNote(String n) {
        this.note = n;
    }

    @Override
    public String toString() {
        return "Patient{" + getUsername() + "," + getName() + "}";
    }

    // prints patient name
    @Override
    public void displayInfo() {
        System.out.println("Patient " + getName());
    }
}
