package me.aloic.lazybotppplus.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(HttpStatus.NOT_FOUND)
public class InvalidScoreException extends RuntimeException
{
    public InvalidScoreException(String message) {
        super(message);
    }

    public InvalidScoreException(Throwable throwable) {
        super(throwable);
    }

    public InvalidScoreException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
