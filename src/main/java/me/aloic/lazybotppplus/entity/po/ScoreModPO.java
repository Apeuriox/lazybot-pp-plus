package me.aloic.lazybotppplus.entity.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@TableName("score_mods")
@AllArgsConstructor
@NoArgsConstructor
public class ScoreModPO
{
    private Long scoreId;
    @TableField("mod")
    private String mod;

}
