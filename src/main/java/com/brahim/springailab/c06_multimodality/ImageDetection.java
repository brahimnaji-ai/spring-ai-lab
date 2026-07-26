package com.brahim.springailab.c06_multimodality;


import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/image")
public class ImageDetection {
    @Value("classpath:/images/img.png")
    private Resource image;
    private final ChatClient chatClient;
    private final String CHAT_CLIENT_BEAN_NAME = "geminiChatClient";

    public ImageDetection(@Qualifier(CHAT_CLIENT_BEAN_NAME) ChatClient chatClient){
        this.chatClient = chatClient;
    }

    @GetMapping("/image-to-text")
    public String imageToText(){
        return chatClient.prompt()
                .user(u->u
                        .text("Can you please describe what you see in the following image?")
                        .media(MimeTypeUtils.IMAGE_PNG, image)
                )
                .call()
                .content();
    }
}
