package me.aloic.lazybotppplus.interceptor;

import lombok.extern.slf4j.Slf4j;
import me.aloic.lazybotppplus.entity.WebResult;
import me.aloic.lazybotppplus.exception.InvalidScoreException;
import me.aloic.lazybotppplus.exception.LazybotRuntimeException;
import me.aloic.lazybotppplus.exception.PlayerNotFoundException;
import me.aloic.lazybotppplus.util.ResultUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice

public class GlobalExceptionHandler {


    @ExceptionHandler(InvalidScoreException.class)
    public ResponseEntity<WebResult> handleScoreNotFound(InvalidScoreException ex) {
        log.warn("Score not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResultUtil.notFound(ex.getMessage()));
    }

    @ExceptionHandler(PlayerNotFoundException.class)
    public ResponseEntity<WebResult> handlePlayerNotFound(PlayerNotFoundException ex) {
        log.warn("Player did not initialize: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResultUtil.notFound(ex.getMessage()));
    }

    @ExceptionHandler(LazybotRuntimeException.class)
    public ResponseEntity<WebResult> handleRuntimeException(LazybotRuntimeException ex) {
        log.warn("Run time exception: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResultUtil.error(ex.getMessage()));
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<WebResult> handleNullPointer(NullPointerException ex) {
        log.error("Null pointer", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResultUtil.error("Server internal error, null pointer"));
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<WebResult> handleGeneralException(Exception ex) {
        log.error("Unknown server error", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResultUtil.error("lol server goes boooom"));
    }
}