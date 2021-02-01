package lk.ac.mrt.cse.dbs.simpleexpensemanager.data.exception;

public class AccountBalanceInsufficientException extends Exception {
    public AccountBalanceInsufficientException(String detailMessage) {
        super(detailMessage);
    }

    public AccountBalanceInsufficientException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }
}
