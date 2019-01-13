package ua.org.ubts.library.exception;

public class UserAlreadyExistsException extends ConflictException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }

}
