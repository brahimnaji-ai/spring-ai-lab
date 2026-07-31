package com.brahim.springailab.c02_advisors;


import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/advisors")
public class AdvisorsController {
    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final String CHAT_CLIENT_BEAN_NAME = "geminiChatClient";
;
    public AdvisorsController(@Qualifier(CHAT_CLIENT_BEAN_NAME) ChatClient chatClient,
                                ChatMemory chatMemory) {
        this.chatClient = chatClient;
        this.chatMemory = chatMemory;
    }

    /**
     * Advisors
     * - The Advisors API provides a flexible and powerful way to intercept,
     * modify, and enhance AI-driven interactions in your Spring applications.
     * - A common pattern when calling an AI model with user text is to append
     * or augment the prompt with `contextual` data.
     * This contextual data can be of different types. Common types include:
         * 1. Your own data: This is data the AI model hasn’t been trained on.
         * Even if the model has seen similar data, the appended contextual data
         * takes precedence in generating the response.
         * 2. Conversational history: The chat model’s API is stateless.
         * If you tell the AI model your name, it won’t remember it in subsequent interactions.
         * Conversational history must be sent with each request to ensure previous interactions
         * are considered when generating a response
     * */

    /**
     * The order in which advisors are added to the chain is crucial,
     * as it determines the sequence of their execution.
     * Each advisor modifies the prompt or the context in some way,
     * and the changes made by one advisor are passed on to the next in the chain.
     * */



    @GetMapping("/simple-logger")
    public String simpleLogger(@RequestParam(defaultValue = "Hello") String userText) {
        return chatClient.prompt()
                .advisors(new SimpleLoggerAdvisor())
                .user(userText)
                .call()
                .content();
    }



    /**
     * You can customize what data from AdvisedRequest and ChatResponse is logged by
     * using the following constructor:
         * SimpleLoggerAdvisor(
         *     Function<ChatClientRequest, String> requestToString,
         *     Function<ChatResponse, String> responseToString,
         *     int order
         * )
     * */

    @GetMapping("/custom-logger")
    public String customLogger(@RequestParam(defaultValue = "Hello") String input) {
        /**This allows you to tailor the logged information to your specific needs.*/
        var customLogger = new SimpleLoggerAdvisor(
                request -> "Custom request: "+request.prompt().getUserMessage(),
                chatResponse -> "Custom response: "+chatResponse.getResult().getOutput(),
                0
        );
        return chatClient.prompt()
                .advisors(customLogger)
                .user(input)
                .call()
                .content();
    }

    /**
     * Logging Advisor
     * We can implement a simple logging advisor that logs the ChatClientRequest
     * before and the ChatClientResponse after the call to the next advisor in the chain.
     * Note that the advisor only observes the request and response and does not modify them.
     * This implementation support both non-streaming and streaming scenarios.
     * */

    /**
     * Re-Reading (Re2) Advisor
     * The "Re-Reading Improves Reasoning in Large Language Models" article introduces a
     * technique called Re-Reading (Re2) that improves the reasoning capabilities of Large
     * Language Models. The Re2 technique requires augmenting the input prompt like this:
         * {Input_Query}
         * Read the question again: {Input_Query}
     * */

    @GetMapping("/re-reading")
    public String re2(@RequestParam(defaultValue = "Hello") String message) {
        // NOTE:: ALL THESE ADVISORS IS PRE REQUEST, MAKE THEM AS A DEFAULT IN CHAT CLIENT CONFIG,
        // TO INVOKE THEM IN EVERY REQUEST
        return chatClient.prompt()
                .user(message)
                .advisors(
                        new CustomSimpleLoggerAdvisor(0),
                        new ReReadingAdvisor().withOrder(10),
                        new CustomSimpleLoggerAdvisor(15)
                )
                .call()
                .content();
    }

    

    /**
     *  MessageChatMemoryAdvisor is one of the easiest advisors to understand because it injects
     *  previous messages into the prompt before the model is called and stores the new messages
     *  after the response.
     *  For learning, we use the in-memory implementation.
     *  Production: Replace this with a persistent implementation (JDBC, Cassandra, Redis, etc.)
     *  so conversations survive application restarts.
     *  */

    @GetMapping("/in-memory-advisor")
    public ChatResponse memory (
            @RequestParam(defaultValue = "Hi, I'm Brahim") String message,
            @RequestParam String conversationId
    ){
        var memoryAdvisor = MessageChatMemoryAdvisor.builder(chatMemory)
                .build();
        return chatClient.prompt()
                .user(message)
                .advisors(a->a
                        .advisors(memoryAdvisor)
                        .param(ChatMemory.CONVERSATION_ID, conversationId)
                )
                .call()
                .chatResponse();
    }



    /**
     * A quick simple example about tools, though isn't our concern in this section
     * */
    @GetMapping("/tool")
    public String tool(@RequestParam(defaultValue = "Hello") String input) {
        var customLogger = new SimpleLoggerAdvisor(
                request -> "Custom request: "+request.prompt().getUserMessage(),
                chatResponse -> "Custom response: "+chatResponse.getResult().getOutput(),
                0
        );
        return chatClient.prompt()
                .advisors(customLogger)
                .tools(new WeatherTools())
                .user(input)
                .call()
                .content();
    }
}
