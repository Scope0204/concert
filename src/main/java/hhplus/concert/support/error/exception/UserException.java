package hhplus.concert.support.error.exception;

public abstract class UserException extends RuntimeException {
    public UserException(String errorMessage) {
        super(errorMessage);
    }

    public static class UserNotFound extends UserException {
        public UserNotFound() {
            super("User not found");
        }
    }
}