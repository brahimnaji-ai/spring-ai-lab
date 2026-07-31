package com.brahim.springailab.config.chat_memory_repository;

import org.springframework.ai.chat.memory.repository.redis.RedisChatMemoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import redis.clients.jedis.RedisClient;

@Configuration
@Profile("redis")
public class RedisRepositoryConfig {

    @Bean
    public RedisChatMemoryRepository redisChatMemoryRepository(RedisClient redisClient){
        return RedisChatMemoryRepository.builder()
                .jedisClient(redisClient)
                .build();
    }
}
