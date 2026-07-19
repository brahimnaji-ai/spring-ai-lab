package com.brahim.springailab.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ChatClientConfig {

    /** Annotate as my primary bean
     * that why it's called default...*/
    @Bean
    @Primary
    public ChatClient defaultChatClient(ChatClient.Builder builder){
        return builder.build();
    }
}
