package me.aloic.lazybotppplus.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;


@ResponseStatus(HttpStatus.NOT_FOUND)
public class PlayerNotFoundException extends RuntimeException
{
    public PlayerNotFoundException(String message) {
        super(message);
    }

    public PlayerNotFoundException(Throwable throwable) {
        super(throwable);
    }

    public PlayerNotFoundException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
