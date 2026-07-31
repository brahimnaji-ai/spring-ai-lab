package com.brahim.springailab.config.chat_memory;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("memory")
public class InMemoryChatMemoryConfig {

    /**
     * MessageWindowChatMemory is now the recommended in-memory chat memory implementation.
     * By default, it uses an InMemoryChatMemoryRepository internally if you don't provide
     * another repository.
     * */

    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .maxMessages(10)
                .build();
    }
}
