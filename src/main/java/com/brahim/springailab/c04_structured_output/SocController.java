package com.brahim.springailab.c04_structured_output;


import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/soc")
@Slf4j
public class SocController {
    private final ChatClient chatClient;
    private final ChatModel chatModel;
    private final String CHAT_CLIENT_BEAN_NAME = "geminiChatClient";


    public SocController(@Qualifier(CHAT_CLIENT_BEAN_NAME) ChatClient chatClient, ChatModel chatModel) {
        this.chatClient = chatClient;
        this.chatModel = chatModel;
    }


    /**
     * DOC:: <a href="https://docs.spring.io/spring-ai/reference/api/structured-output-converter.html">
     *       Structured Output converter documentation
     *       </a>
     */

    /**
     * Available Converters; Currently, Spring AI provides:
     * AbstractConversionServiceOutputConverter,
     * AbstractMessageOutputConverter,
     * BeanOutputConverter,
     * MapOutputConverter
     * and ListOutputConverter implementations.
     */

    record Laptop(String name, String type){}
    @GetMapping("/bean-output-converter")
    public String beanOutputConverter(@RequestParam(defaultValue = "HP") String company) {
        // use-case1: high-level
        // remove if statement + add return to visualize result.
        if(false){
            Laptop laptop1 = chatClient.prompt()
                    .user(u->u.text("Generate one laptop of  {name}.")
                            .param("name", company)
                    )
                    .call()
                    .entity(Laptop.class);
        }
        // use-case2: low-level
        BeanOutputConverter<Laptop> laptopBeanOutputConverter = new
                BeanOutputConverter<>(Laptop.class);
        String format = laptopBeanOutputConverter.getFormat();
        log.info("Format: {}",format);
        var inc = "HP";
        var template = """
                Generate one laptop of {name}.
                {format}
                """;
        Generation generation = chatModel.call(
                PromptTemplate.builder()
                        .template(template)
                        .variables(Map.of("name", inc, "format", format))
                        .build()
                        .create()
        ).getResult();

        assert Objects.requireNonNull(generation).getOutput().getText() != null;
        Laptop laptop2 = laptopBeanOutputConverter.convert(generation.getOutput().getText());
        return laptop2.toString();

    }
}
