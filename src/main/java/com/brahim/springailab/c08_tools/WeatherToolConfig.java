package com.brahim.springailab.c08_tools;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.function.FunctionToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class WeatherToolConfig {
    WeatherService service = new WeatherService();
    @Bean
    ToolCallback currentWeather(){
        return FunctionToolCallback.builder("currentWeather", service)
                .description("Get the weather in a location.")
                .inputType(WeatherService.WeatherRequest.class)
                .build();
    }
}
