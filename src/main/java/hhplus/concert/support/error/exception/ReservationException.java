package hhplus.concert.support.error.exception;

public abstract class ReservationException extends RuntimeException {
    public ReservationException(String errorMessage) {
        super(errorMessage);
    }

    public static class ReservationNotFound extends ReservationException {
        public ReservationNotFound() {
            super("Reservation not found");
        }
    }
}