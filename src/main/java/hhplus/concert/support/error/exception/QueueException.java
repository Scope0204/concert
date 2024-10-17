package hhplus.concert.support.error.exception;

public abstract class QueueException extends RuntimeException {
    public QueueException(String errorMessage) {
        super(errorMessage);
    }

    public static class QueueNotFound extends QueueException {
        public QueueNotFound() {
            super("Queue not found");
        }
    }

    public static class QueueNotAllowed extends QueueException {
        public QueueNotAllowed() {
            super("Queue is not active");
        }
    }
}
