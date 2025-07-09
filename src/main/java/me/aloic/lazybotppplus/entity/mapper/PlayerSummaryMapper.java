package me.aloic.lazybotppplus.entity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import me.aloic.lazybotppplus.entity.po.PlayerSummaryPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PlayerSummaryMapper extends BaseMapper<PlayerSummaryPO>
{
    void insertBatch(List<PlayerSummaryPO> players);
    PlayerSummaryPO selectById(Long id);
    List<PlayerSummaryPO> selectAll();
    List<PlayerSummaryPO> selectPlayersWithLimit(@Param("offset") int offset, @Param("limit") int limit);
    Integer selectPlayerCount();
}
