package com.brahim.springailab.config.chat_memory_repository;


import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
@Profile("jdbc")
public class JdbcRepositoryConfig {

    @Bean
    public ChatMemoryRepository ChatMemoryRepository (DataSource dataSource){
        return JdbcChatMemoryRepository.builder()
                .dataSource(dataSource)
                .build();
    }
}
