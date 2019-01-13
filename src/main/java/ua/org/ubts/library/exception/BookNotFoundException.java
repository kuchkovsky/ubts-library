package ua.org.ubts.library.exception;

public class BookNotFoundException extends DatabaseItemNotFoundException {

    public BookNotFoundException(String message) {
        super(message);
    }

}
