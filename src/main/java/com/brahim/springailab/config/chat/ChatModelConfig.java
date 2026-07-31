package com.brahim.springailab.config.chat;


import com.google.genai.Client;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
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
        Client client = Client.builder()
                .apiKey(this.apikey)
                .build();
        GoogleGenAiChatOptions options = GoogleGenAiChatOptions.builder()
                .model("gemini-3.1-flash-lite")
                .temperature(0.7)
                .maxOutputTokens(2048)
                .build();
        return GoogleGenAiChatModel.builder()
                .genAiClient(client)
                .options(options)
                .build();
    }

}
