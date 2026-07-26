package com.brahim.springailab.c07_memory;


import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class InMemoryController {

    private final ChatClient chatClient;
    private final ChatClient statelessChatClient;

    public InMemoryController(ChatClient.Builder builder, ChatMemory chatMemory) {
        this.statelessChatClient = builder.build();
        this.chatClient = builder
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory)
                                .build()
                )
                .build();
    }

    /**
     * Large language models (LLMs) are stateless, meaning they do not retain information
     * about previous interactions. This can be a limitation when you want to maintain context
     * or state across multiple interactions.</br>
     * An example endpoint demonstrating that a ChatClient call is <b>stateless</b>.
     * */

    @GetMapping("/chat")
    public String problem(@RequestParam(defaultValue = "Hi, My name is Brahim") String prompt){
        return statelessChatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    /**
     * To address this, Spring AI provides chat memory features that allow you to store
     * and retrieve information across multiple interactions with the LLM.
     * Before choosing a memory type, it’s essential to understand the difference between chat memory and chat history.
     * - Chat Memory:  The information that a large-language model retains and uses
     * to maintain contextual awareness throughout a conversation.
     * - Chat History. The entire conversation history, including all messages exchanged
     * between the user and the model.
     * */


    /**
     * The <B>ChatMemory.CONVERSATION_ID</B> parameter is required for all memory advisors.
     * Calls that omit this parameter will throw an <B>IllegalArgumentException</B> at runtime.
     * There is no default conversation ID.
     * */

    @GetMapping("/in-memory")
    public String inMemory(
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
