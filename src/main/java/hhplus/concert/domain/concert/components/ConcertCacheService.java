package hhplus.concert.domain.concert.components;

import hhplus.concert.support.config.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Service
public class ConcertCacheService {
    @CacheEvict(
            cacheNames = {CacheConfig.CACHE_TEN_MIN},
            key = "'v1-concert-' + #token"
    )
    public void evictConcertCache(String token) {}

    @CacheEvict(
            cacheNames = {CacheConfig.CACHE_TEN_MIN},
            key = "'v1-concert-' + #concertId"
    )
    public void evictConcertScheduleCache(Long concertId) {}

    @CacheEvict(
            cacheNames = {CacheConfig.CACHE_ONE_MIN},
            key = "'v1-concert-' + #concertId + '-scheduleId-' + #scheduleId"
    )
    public void evictSeatCache(Long concertId, Long scheduleId) {}
}
