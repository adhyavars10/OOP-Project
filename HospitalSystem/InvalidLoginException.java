// custom exception for login errors
public class InvalidLoginException extends Exception {

    public InvalidLoginException() {
        super("Invalid login");
    }

    public InvalidLoginException(String msg) {
        super(msg);
    }

    public InvalidLoginException(String msg, Throwable t) {
        super(msg, t);
    }
}
