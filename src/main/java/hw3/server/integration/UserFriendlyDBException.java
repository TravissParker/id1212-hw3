package hw3.server.integration;

public class UserFriendlyDBException extends Exception {

    public UserFriendlyDBException(String msg) {
        super(msg);
    }

    public UserFriendlyDBException(String msg, Exception e) {
        super(msg, e);
    }
}
