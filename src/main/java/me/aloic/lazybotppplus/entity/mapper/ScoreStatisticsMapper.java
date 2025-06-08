package me.aloic.lazybotppplus.entity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import me.aloic.lazybotppplus.entity.po.BeatmapPO;
import me.aloic.lazybotppplus.entity.po.ScoreStatisticsPO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ScoreStatisticsMapper extends BaseMapper<ScoreStatisticsPO>
{
    void insertBatch(List<ScoreStatisticsPO> stat);
    void deleteByScoreId(Long scoreId);
    void insertSingle(ScoreStatisticsPO stat);
}
