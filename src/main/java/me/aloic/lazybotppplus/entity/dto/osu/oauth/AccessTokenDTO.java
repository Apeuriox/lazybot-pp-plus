package me.aloic.lazybotppplus.entity.dto.osu.oauth;

import lombok.Data;

import java.io.Serializable;

@Data
public class AccessTokenDTO implements Serializable {

    private String token_type;

    private Integer expires_in;

    private String access_token;

    private String refresh_token;

}
