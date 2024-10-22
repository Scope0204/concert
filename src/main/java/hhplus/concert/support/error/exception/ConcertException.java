package hhplus.concert.support.error.exception;

public abstract class ConcertException extends RuntimeException {
    public ConcertException(String errorMessage) {
        super(errorMessage);
    }

    public static class ConcertNotFound extends ConcertException {
        public ConcertNotFound() {
            super("Concert not found");
        }
    }

    public static class ConcertUnavailable extends ConcertException {
        public ConcertUnavailable() {
            super("Concert is unavailable");
        }
    }

    public static class ConcertScheduleNotFound extends ConcertException {
        public ConcertScheduleNotFound() {
            super("Concert Schedule is not found");
        }
    }

    public static class ConcertSeatNotFound extends ConcertException {
        public ConcertSeatNotFound() {
            super("Concert Seat is not found");
        }
    }
}
