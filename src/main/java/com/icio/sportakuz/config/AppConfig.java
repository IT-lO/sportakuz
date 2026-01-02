package com.icio.sportakuz.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;

@Configuration
@EnableScheduling
@ComponentScan(
        basePackages = "com.icio.sportakuz",
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Controller.class)
        }
)
public class AppConfig {
}