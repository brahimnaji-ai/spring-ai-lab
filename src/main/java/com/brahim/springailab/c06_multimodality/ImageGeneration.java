package com.brahim.springailab.c06_multimodality;


import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.image.ImageMessage;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.stabilityai.StabilityAiImageModel;
import org.springframework.ai.stabilityai.api.StabilityAiImageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Slf4j
public class ImageGeneration {

    @Value("${spring.ai.stabilityai.image.model}")
    private String model;
    /**Won't work, unless you have valid api key [credits].*/
    private final StabilityAiImageModel stabilityaiImageModel;

    public ImageGeneration(StabilityAiImageModel stabilityaiImageModel){
        this.stabilityaiImageModel = stabilityaiImageModel;
    }

    @GetMapping("/generate-image")
    public ResponseEntity<Map<String, String>> textToImage(
            @RequestParam(defaultValue = "A beautiful sunset over mountains.")String prompt
    ){
        System.out.println(this.model);
        ImageResponse response = stabilityaiImageModel.call(
                new ImagePrompt(
                        new ImageMessage(prompt),
                        StabilityAiImageOptions.builder()
                                .stylePreset("cinematic")
                                .height(1024)
                                .width(1024)
                                .build()
                )
        );
        String url = response.getResult().getOutput().getUrl();
        return ResponseEntity.ok(
                Map.of(
                        "prompt", prompt,
                        "url", url
                )
        );
    }
}
