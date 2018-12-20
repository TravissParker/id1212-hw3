package hw3.server.integration;

public class DBException extends Exception {

    public DBException() {}

    public DBException(String msg) {
        super(msg);
    }

    public DBException(String msg, Exception e) {
        super(msg, e);
    }
}
