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
public class RedisMemoryController {
    private final ChatClient chatClient;

    public RedisMemoryController(
            ChatClient.Builder builder,
            @Qualifier("redisChatMemory") ChatMemory chatMemory
    ){
        this.chatClient = builder
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory)
                        .build())
                .build();
    }


    @GetMapping("/redis-memory")
    public String redisMemory(
            @RequestParam(defaultValue = "Hi, My name is Brahim") String prompt,
            @RequestParam(defaultValue = "001") String conversationId) {

        return chatClient.prompt()
                .user(prompt)
                .advisors(advisor -> advisor.param(
                        ChatMemory.CONVERSATION_ID,
                        conversationId))
                .call()
                .content();
    }

}
