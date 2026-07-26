package com.brahim.springailab.config;


import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class MemoryConfig {

    @Bean
    @Primary
    public ChatMemory inMemoryChatMemory(){
        /**
         * MessageWindowChatMemory is now the recommended in-memory chat memory implementation.
         * By default, it uses an InMemoryChatMemoryRepository internally if you don't provide
         * another repository.
         * */
        return MessageWindowChatMemory.builder()
                .maxMessages(10)
                .build();
    }


    @Bean
    public ChatMemory jdbcChatMemory(
            @Qualifier("jdbcChatMemoryRepository")
            ChatMemoryRepository repository
    ){

        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(repository)
                .maxMessages(10)
                .build();
    }


    @Bean
    public ChatMemory redisChatMemory(
            @Qualifier("redisChatMemoryRepository")
            ChatMemoryRepository repository
    ){
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(repository)
                .maxMessages(10)
                .build();
    }
}
