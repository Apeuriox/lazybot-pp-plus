package me.aloic.lazybotppplus.entity.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import me.aloic.lazybotppplus.entity.po.BeatmapPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BeatmapMapper extends BaseMapper<BeatmapPO>
{
    void insertBatchUpsert(List<BeatmapPO> map);
    void insertBatchIgnoreDuplicate(List<BeatmapPO> map);
}
