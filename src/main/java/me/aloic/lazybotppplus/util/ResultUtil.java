package me.aloic.lazybotppplus.util;
import me.aloic.lazybotppplus.entity.WebResult;
import org.springframework.stereotype.Component;

/**
 * 封装http请求相关方法
 */
@Component
public class ResultUtil
{
    private static final Integer SUCCESS_CODE = 200;

    private static final Integer ERROR_CODE = 400;

    private static final Integer NOT_FOUND_CODE = 404;

    private static final Integer TEAPOT_CODE = 418;

    public static WebResult success(Object o){
        return new WebResult(SUCCESS_CODE, o, "Success");
    }
    public static WebResult success(Object o,String msg){
        return new WebResult(SUCCESS_CODE, o, msg);
    }

    public static WebResult error(String msg){
        return new WebResult(ERROR_CODE, null, msg);
    }
    public static WebResult notFound(String msg){
        return new WebResult(NOT_FOUND_CODE, null, msg);
    }

    public static WebResult error(Integer code,String msg){
        return new WebResult(code, null, msg);
    }
    public static WebResult notify(Integer code,String msg){
        return new WebResult(code, null, msg);
    }

}
