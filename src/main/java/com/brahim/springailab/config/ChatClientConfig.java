package com.brahim.springailab.config;

import com.google.genai.Client;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.springframework.ai.chat.client.ChatClient.builder;

@Configuration
public class ChatClientConfig {
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
    @Bean
    public ChatClient geminiChatClient(GoogleGenAiChatModel model){
        return ChatClient.create(model);
    }
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
//    @Bean
//    public ChatClient anthropicChatClient(AnthropicChatModel model){return ChatClient.create(model);}
//    @Bean
//    public ChatClient ollamaChatClient(OllamaChatModel model){return ChatClient.create(model);}
}
