package com.example.lly.common.queue;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.io.Serializable;
import java.time.Duration;

@Configuration
public class RedisConfig extends CachingConfigurerSupport {

    private final Duration livingTime = Duration.ofDays(1);  //������Ч��һ��
    private final Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer;
    private final RedisSerializer<String> redisSerializer;


    public RedisConfig() {
        redisSerializer = new StringRedisSerializer();
        //查询缓存配置 👇
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        this.jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        this.jackson2JsonRedisSerializer.setObjectMapper(objectMapper);
    }

    @Bean
    public KeyGenerator keyGenerator() {
        //Define the existing policy of cache data
        //Lambda expression
        return (target, method, params) -> {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(target.getClass().getName());
            stringBuilder.append(method.getName());
            for (Object object : params) {
                stringBuilder.append(object.toString());
            }
            return stringBuilder.toString();
        };
    }


    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        //redis缓存属性配置
        RedisCacheConfiguration redisCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                                            .entryTtl(this.livingTime)
//                                            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(this.redisSerializer))
//                                            .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(this.jackson2JsonRedisSerializer))
                                            .disableCachingNullValues();

        //将其实例创建出来成为一个Bean
        return RedisCacheManager.builder(connectionFactory).cacheDefaults(redisCacheConfig).build();
    }


    @Bean
    public RedisTemplate<String, Serializable> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Serializable> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);
//        redisTemplate.setKeySerializer(this.jackson2JsonRedisSerializer);
//        redisTemplate.setHashKeySerializer(this.jackson2JsonRedisSerializer);
//        redisTemplate.setHashValueSerializer(this.jackson2JsonRedisSerializer);
//        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }


    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate(factory);
        stringRedisTemplate.setKeySerializer(this.jackson2JsonRedisSerializer);
        stringRedisTemplate.setHashKeySerializer(this.jackson2JsonRedisSerializer);
        stringRedisTemplate.setHashValueSerializer(this.jackson2JsonRedisSerializer);
        stringRedisTemplate.afterPropertiesSet();
        return stringRedisTemplate;
    }


}
