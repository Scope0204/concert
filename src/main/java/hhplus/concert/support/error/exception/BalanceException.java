package hhplus.concert.support.error.exception;

public abstract class BalanceException extends RuntimeException {
    public BalanceException(String errorMessage) {
        super(errorMessage);
    }

    public static class BalanceNotFound extends BalanceException {
        public BalanceNotFound() {
            super("Balance not found");
        }
    }

    public static class BalaceAmountInvalid extends BalanceException {
        public BalaceAmountInvalid() { super("Invalid charge amount"); }
    }
}