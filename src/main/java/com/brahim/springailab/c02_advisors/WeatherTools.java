package com.brahim.springailab.c02_advisors;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class WeatherTools {

    private final Map<String, Integer> WEATHER_CONSTANTS = Map.of(
      "Casablanca", 20,
      "Fes", 30,
      "Rabat", 20,
      "Temara", 26,
      "Meknes", 32,
      "Tanger", 19,
      "Tetouan", 21,
      "Oujda", 31
    );


    @Tool(description = "Get the weather for a city")
    public Integer getCurrentWeather(String city){
        return WEATHER_CONSTANTS.getOrDefault(city, 20);
    }
}
