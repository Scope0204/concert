package hhplus.concert.support.error.exception;

public abstract class PaymentException extends RuntimeException {
    public PaymentException(String errorMessage) {
        super(errorMessage);
    }

    public static class InvalidRequest extends PaymentException {
        public InvalidRequest() {
            super("Invalid payment request");
        }
    }
    public static class InvalidPaymentAmount extends PaymentException {
        public InvalidPaymentAmount() {
            super("Invalid payment amount");
        }
    }
}