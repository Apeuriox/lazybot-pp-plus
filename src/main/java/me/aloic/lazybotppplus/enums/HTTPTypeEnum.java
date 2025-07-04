package me.aloic.lazybotppplus.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum HTTPTypeEnum
{
    GET("get"),
    POST("post"),
    DELETE("delete");

    private String type;
}
