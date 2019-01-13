package ua.org.ubts.library.exception;

public class UserNotFoundException extends DatabaseItemNotFoundException {

    public UserNotFoundException(String message) {
        super(message);
    }

}
