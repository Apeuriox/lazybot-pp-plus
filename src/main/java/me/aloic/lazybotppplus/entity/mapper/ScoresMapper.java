package me.aloic.lazybotppplus.entity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import me.aloic.lazybotppplus.entity.dto.lazybot.ScorePerformanceDTO;
import me.aloic.lazybotppplus.entity.po.ScorePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;


@Mapper
public interface ScoresMapper extends BaseMapper<ScorePO> {
    ScorePerformanceDTO selectScoreDetailById(@Param("id") Long scoreId);
    List<ScorePO> selectTopScoresByPlayerIdAndDimension(@Param("playerId") Long playerId,
                                                      @Param("dimension") String dimension);

    void insertBatch(List<ScorePO> scores);
    void deleteById(Long id);
    ScorePO selectByPlayerIdAndBeatmapId(Long playerId, Integer beatmapId);
    List<ScorePO> selectBestScoresByPlayerAndBeatmapIds(@Param("playerId") Long playerId, @Param("beatmapIds") Set<Long> beatmapIds);

    List<ScorePerformanceDTO> selectBestScoresInSingleDimension(
            @Param("playerId") Long playerId,
            @Param("dimension") String dimensionColumn,
            @Param("limit") Integer limit,
            @Param("offset") Integer offset
    );
}
