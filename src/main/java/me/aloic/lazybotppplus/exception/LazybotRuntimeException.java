package me.aloic.lazybotppplus.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
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
