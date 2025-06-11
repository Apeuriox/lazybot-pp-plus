package me.aloic.lazybotppplus.entity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import me.aloic.lazybotppplus.entity.po.ApiClientPO;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface ApiClientMapper extends BaseMapper<ApiClientPO>
{
    ApiClientPO selectByClientId(Integer clientId);
}
