package me.aloic.lazybotppplus.entity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import me.aloic.lazybotppplus.entity.po.PlayerSummaryPO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PlayerSummaryMapper extends BaseMapper<PlayerSummaryPO>
{
    void insertBatch(List<PlayerSummaryPO> players);
    PlayerSummaryPO selectById(Long id);
}
