package com.brahim.springailab.config;


import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MemoryConfig {

    @Bean
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
}
