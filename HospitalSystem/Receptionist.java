public class Receptionist extends User {

    // constructor
    public Receptionist(String u, String pwd, String nm) {
        super(u, pwd, "receptionist", nm);
    }

    // extra stuff
    private String deskCode;

    public String getDeskCode() {
        return deskCode;
    }

    public void setDeskCode(String d) {
        this.deskCode = d;
    }

    @Override
    public String toString() {
        return "Receptionist{" + getUsername() + "," + getName() + "}";
    }
}
