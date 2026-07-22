package com.brahim.springailab.config;

import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ChatClientConfig {

    @Bean
    @Primary
    public ChatClient geminiChatClient(GoogleGenAiChatModel model){
        return ChatClient.create(model);
    }
    @Bean
    public ChatClient anthropicChatClient(AnthropicChatModel model){return ChatClient.create(model);}
    @Bean
    public ChatClient ollamaChatClient(OllamaChatModel model){return ChatClient.create(model);}
}
