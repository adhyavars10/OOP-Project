public class User {

    private String username;
    private String password;
    private String role;
    private String name;

    // constructor
    public User(String u, String p, String r, String n) {
        this.username = u;
        this.password = p;
        this.role = r;
        this.name = n;
    }

    // check if login is correct
    public boolean authenticate(String u, String p) {
        if (u == null || p == null) return false;
        return username.equals(u) && password.equals(p);
    }

    // getters
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public String getName() {
        return name;
    }

    // setters
    public void setUsername(String s) {
        this.username = s;
    }

    public void setPassword(String s) {
        this.password = s;
    }

    public void setRole(String s) {
        this.role = s;
    }

    public void setName(String s) {
        this.name = s;
    }

    @Override
    public String toString() {
        return username + "," + role + "," + name;
    }
}
