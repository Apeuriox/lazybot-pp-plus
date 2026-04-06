package me.aloic.lazybotppplus.service;

import org.springframework.transaction.annotation.Transactional;

public interface BeatmapService
{
    @Transactional
    void updatedBeatmapCache();
}
