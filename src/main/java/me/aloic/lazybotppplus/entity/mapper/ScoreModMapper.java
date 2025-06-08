package me.aloic.lazybotppplus.entity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import me.aloic.lazybotppplus.entity.po.ScoreModPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ScoreModMapper extends BaseMapper<ScoreModPO>
{
    List<String> selectModsByScoreId(@Param("scoreId") Long scoreId);
    void insertBatch(List<ScoreModPO> mod);
    void deleteByScoreId(Long scoreId);
    List<ScoreModPO> selectByScoreIds(@Param("scoreIds") List<Long> scoreIds);
}
