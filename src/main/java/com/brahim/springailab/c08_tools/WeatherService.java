package com.brahim.springailab.c08_tools;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;
@Slf4j
public class WeatherService implements Function<WeatherService.WeatherRequest, WeatherService.WeatherResponse> {
    @Override
    public WeatherResponse apply(WeatherRequest weatherRequest) {
        log.info("Apply function is called.");
        return new WeatherResponse(30.0, Unit.C);
    }
    public enum Unit{C, F};
    public record WeatherRequest(String location, Unit unit){};
    public record WeatherResponse(double temp, Unit unit){};
}

