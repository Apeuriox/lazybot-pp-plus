package me.aloic.lazybotppplus.exception;

public class LazybotRuntimeException extends RuntimeException
{
    public LazybotRuntimeException(String message) {
        super(message);
    }

    public LazybotRuntimeException(Throwable throwable) {
        super(throwable);
    }

    public LazybotRuntimeException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
