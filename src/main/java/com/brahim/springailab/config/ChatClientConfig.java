package com.brahim.springailab.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static org.springframework.ai.chat.client.ChatClient.builder;

@Configuration
public class ChatClientConfig {


    @Bean
    @Primary
    public ChatClient geminiChatClient(GoogleGenAiChatModel model){
        return ChatClient.create(model);
    }


    @Bean
    public ChatClient ChatClientWithDefaultSystemWithParameters(GoogleGenAiChatModel model){
        return builder(model)
                .defaultSystem("You are a friendly chat bot that" +
                        " answers question in the voice of a {voice}")
                .build();
    }

    @Bean
    public ChatClient ChatClientWithDefaultSystem(GoogleGenAiChatModel model){
        return builder(model)
                .defaultSystem("You are a friendly chat bot that answers question" +
                        " in the voice of a Pirate")
                .build();
    }

}
