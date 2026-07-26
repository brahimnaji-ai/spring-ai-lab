package com.brahim.springailab.config;


import com.google.genai.Client;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ChatModelConfig {
    Dotenv dotenv = Dotenv.load();

    private String apikey = dotenv.get("GENAI_API_KEY");
    @Bean
    @Primary
    public GoogleGenAiChatModel googleGenAiChatModel(){
        return GoogleGenAiChatModel.builder()
                .genAiClient(Client.builder()
                        .apiKey(this.apikey)
                        .build())
                .build();
    }

}
