package hhplus.concert.support.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CACHE_ONE_MIN = "cache-one-min";
    public static final long TTL_ONE_MINUTE = 1L;
    public static final String CACHE_TEN_MIN = "cache-ten-min";
    public static final long TTL_TEN_MINUTE = 10L;

    private final ObjectMapper objectMapper;

    public CacheConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * ObjectMapper 에 추가 설정을 적용합니다.
     */
    private ObjectMapper configureObjectMapper() {
        ObjectMapper customizedObjectMapper = objectMapper.copy();

        customizedObjectMapper.registerModule(new JavaTimeModule());
        customizedObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        customizedObjectMapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfBaseType(Object.class)
                        .build(),
                ObjectMapper.DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.PROPERTY
        );

        return customizedObjectMapper;
    }

    /**
     * 캐시 이름 별로 세팅을 위해 RedisCacheManagerBuilderCustomizer 를 선언
     * 모든 캐시에서 동일한 설정을 적용하려는 경우에는 RedisCacheConfiguration 를 빈으로 등록하도록 합니다.
     */
    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        ObjectMapper configureObjectMapper = configureObjectMapper();

        return builder -> builder
                .withCacheConfiguration(CACHE_ONE_MIN, redisCacheConfigurationByTtl(configureObjectMapper, TTL_ONE_MINUTE))
                .withCacheConfiguration(CACHE_TEN_MIN, redisCacheConfigurationByTtl(configureObjectMapper, TTL_TEN_MINUTE));
    }

    private RedisCacheConfiguration redisCacheConfigurationByTtl(ObjectMapper configureObjectMapper, long ttlInMin) {

        return RedisCacheConfiguration.defaultCacheConfig()
                .computePrefixWith(cacheName -> cacheName + "::")
                .entryTtl(Duration.ofMinutes(ttlInMin))
                .disableCachingNullValues()
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer(configureObjectMapper)));
    }
}
