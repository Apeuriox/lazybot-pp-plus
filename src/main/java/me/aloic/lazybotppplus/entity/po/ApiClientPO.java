package me.aloic.lazybotppplus.entity.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("api_client")
public class ApiClientPO
{
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer clientId;
    private String clientSecret;
    private String description;
    private LocalDateTime createdAt;
    public ApiClientPO(Integer clientId, String clientSecret, String description)
    {
        this.clientId=clientId;
        this.clientSecret=clientSecret;
        this.description=description;
        this.createdAt=LocalDateTime.now();
    }
}