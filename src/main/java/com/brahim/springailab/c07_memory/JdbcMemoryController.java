package com.brahim.springailab.c07_memory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class JdbcMemoryController {

    private final ChatClient chatClient;

    public JdbcMemoryController(ChatClient.Builder builder, @Qualifier("jdbcChatMemory") ChatMemory chatMemory) {
        this.chatClient = builder
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory)
                        .build())
                .build();
    }


    @GetMapping("/jdbc-memory")
    public String jdbcMemory(
            @RequestParam(defaultValue = "Hi, My name is Brahim") String prompt,
            @RequestParam(defaultValue = "001") String conversationId
    ){
        return chatClient.prompt()
                .user(prompt)
                .advisors(a->a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }
}
