package com.brahim.springailab.c03_prompts;


import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/prompts")
@Slf4j
public class PromptController {
    @Value("classpath:/prompts/system-message.st")
    private Resource systemResource;

    private final ChatClient chatClient;
    private final String CHAT_CLIENT_BEAN_NAME = "geminiChatClient";
    private String SYSTEM_MESSAGE = """
                You are a Senior Java developer
                Currently building reliable, scalable, and maintainable applications.
                """;
    PromptController(@Qualifier(CHAT_CLIENT_BEAN_NAME) ChatClient chatClient){
        this.chatClient = chatClient;
    }

    public void setSystemMessage(String systemMessage){
        this.SYSTEM_MESSAGE = systemMessage;
    }

    /**
     * The Prompt class functions as a container for an organized series of Message objects
     * and a request ChatOptions. Every Message embodies a unique role within the prompt,
     * differing in its content and intent. These roles can encompass a variety of elements,
     * from user inquiries to AI-generated responses to relevant background information.
     * This arrangement enables intricate and detailed interactions with AI models, as the
     * prompt is constructed from multiple messages, each assigned a specific role to play in
     * the dialogue.
     * */

    /**
     * Please view img.png, it describes prompt structure with diagram and how it works.
     * */




    @GetMapping("/prompt")
    public ResponseEntity<String> Prompt(@RequestParam(defaultValue = "Hello") String message) {

        var prompt = new Prompt(
                new UserMessage(message),
                new SystemMessage(SYSTEM_MESSAGE)
        );
        // You can see in logs, all infos...
        log.info("Role: {}, Message: {}", prompt.getUserMessage().getMetadata().get("messageType"), prompt.getUserMessage().getText());
        log.info("Role: {}, Message : {}", prompt.getSystemMessage().getMetadata().get("messageType"), prompt.getSystemMessage().getText());
        var content = chatClient.prompt(prompt)
                .call()
                .content();
        return ResponseEntity.ok(content);
    }
    @GetMapping("/prompt-template")
    public String promptTemplate(@RequestParam(defaultValue = "Java") String language){


        var promptTemplate = PromptTemplate.builder()
                .template("""
                        You are a Senior {language} developer
                        Currently building reliable, scalable, and maintainable applications.
                        """)
                .build();
        var prompt = promptTemplate.render(Map.of("language", language));
        return chatClient.prompt(prompt)
                .call()
                .content();
    }


    /**
     * This shows how you can build up the Prompt instance by using the <b>SystemPromptTemplate</b>
     * to create a Message with the system role passing in placeholder values.
     * The message with the role <b>user</b> is then combined with the message of the role <b>system</b> to form the prompt.
     * The prompt is then passed to the ChatModel to get a generative response.
     * */

    @GetMapping("/system-prompt-template")
    public String systemPromptTemplate(){
        var userText = """
                Tell me about three famous pirates from the Golden Age of Piracy and why they did.
                Write at least a sentence for each pirate
                """;
        var userMessage = new UserMessage(userText);
        var systemText = """
                You are a helpful AI assistant that helps people find information.
                Your name is {name}
                You should reply to the user's request with your name and also in the style of a {voice}.
                """;
        var name = "GROKXXX"; var voice = "DEEP";
        var systemPromptTemplate = new SystemPromptTemplate(systemText);
        Message systemMessage = systemPromptTemplate.createMessage(
          Map.of("name", name, "voice",voice)
        );

        Prompt prompt = new Prompt(
                systemMessage,
                userMessage
        );
        return chatClient.prompt(prompt)
                .call()
                .content();

    }


    @GetMapping("/custom-prompt-template")
    public String customPromptTemplate(@RequestParam(defaultValue = "Hello") String person,String number){
        // We can make the number of works as a variable in the template.

        SystemMessage systemMessage = new SystemMessage(
          "You are a helpful assistant researcher." +
                  "That help me to fetch people work." +
                  "When user asks you about person's work, you list them in lines."
        );
        PromptTemplate promptTemplate = PromptTemplate.builder()
                .renderer(StTemplateRenderer.builder()
                        .startDelimiterToken('<')
                        .endDelimiterToken('>')
                        .build()
                )
                .template("""
                        Tell me top <number> works of <person>.
                        """)
                .build();
        // I want to leave trace, of user-system messages.
        Message userMessage = promptTemplate.createMessage(Map.of("person", person, "number", number));
        Prompt prompt = new Prompt(
                List.of(
                        userMessage,
                        systemMessage
                )
        );
        log.info(prompt.toString());
        return chatClient.prompt(prompt)
                .call()
                .content();
    }

    /**
     * Using resources instead of raw Strings
     * Spring AI supports the org.springframework.core.io.Resource abstraction,
     * so you can put prompt data in a file that can directly be used in a PromptTemplate.
     * For example, you can define a field in your Spring managed component to retrieve the
     * Resource.
     * NOTE:: everything under src/main/resources is copied onto the classpath when the project is built.
     *
     * <h6>Production Pattern</h6>
     * Instead of creating a SystemPromptTemplate on every request,
     * load it once because the file doesn't change while the application is running.</br>
     *
     *
     * <h6>External templates offer several advantages:</h6></br>
     *
     * - <b>Separation</b> of concerns: prompt engineering is separated from application logic.</br>
     * - <b>Maintainability</b>: long prompts remain readable and easier to edit.</br>
     * - <b>Reusability</b>: the same template can be used across controllers or services.</br>
     * - <b>Version</b> control: prompt changes appear as normal text diffs in Git.</br>
     * - <b>Parameterization</b>: placeholders like {topic} let you reuse one template for many scenarios without duplicating text.</br>
     * */
    @GetMapping("/resource-prompt-template")
    public String resourcePromptTemplate(
            @RequestParam(defaultValue = "security") String specialization,
            @RequestParam(defaultValue = "Explain csrf briefly.") String message
    ) throws IOException {
        log.info(systemResource.getContentAsString(StandardCharsets.UTF_8));
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemResource);
        SystemMessage systemMessage = (SystemMessage) systemPromptTemplate.createMessage(Map.of("specialization", specialization));
        UserMessage userMessage = new UserMessage(message);

        Prompt prompt = new Prompt(
           systemMessage,
           userMessage
        );
        return chatClient.prompt(prompt)
                .call()
                .content();
    }
}
