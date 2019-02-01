package ua.org.ubts.library.exception;

public class CommentNotFoundException extends DatabaseItemNotFoundException {

    public CommentNotFoundException(String message) {
        super(message);
    }

}
