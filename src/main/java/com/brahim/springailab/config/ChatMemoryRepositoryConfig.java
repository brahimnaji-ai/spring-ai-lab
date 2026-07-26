package com.brahim.springailab.config;

import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.memory.repository.redis.RedisChatMemoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import redis.clients.jedis.RedisClient;

import javax.sql.DataSource;

@Configuration
public class ChatMemoryRepositoryConfig {

    @Bean
    @Primary
    public JdbcChatMemoryRepository jdbcChatMemoryRepository (DataSource dataSource){
        return JdbcChatMemoryRepository.builder()
                .dataSource(dataSource)
                .build();
    }


    @Bean
    public RedisChatMemoryRepository redisChatMemoryRepository(RedisClient redisClient){
        return RedisChatMemoryRepository.builder()
                .jedisClient(redisClient)
                .build();
    }
}
