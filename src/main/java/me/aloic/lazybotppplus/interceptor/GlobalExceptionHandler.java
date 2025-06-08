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
        log.warn("成绩未找到: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResultUtil.notFound(ex.getMessage()));
    }

    @ExceptionHandler(PlayerNotFoundException.class)
    public ResponseEntity<WebResult> handlePlayerNotFound(PlayerNotFoundException ex) {
        log.warn("玩家未初始化: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ResultUtil.notFound(ex.getMessage()));
    }

    @ExceptionHandler(LazybotRuntimeException.class)
    public ResponseEntity<WebResult> handleRuntimeException(LazybotRuntimeException ex) {
        log.warn("运行时错误: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResultUtil.error(ex.getMessage()));
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<WebResult> handleNullPointer(NullPointerException ex) {
        log.error("空指针异常", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ResultUtil.error("服务器内部异常：空指针"));
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<WebResult> handleGeneralException(Exception ex) {
        log.error("系统未知异常", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResultUtil.error("服务器爆炸啦，请稍后重试"));
    }
}