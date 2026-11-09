package com.civicassist.config;

import com.google.genai.Client;
import com.google.genai.types.HttpOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogleGenAiConfig {

    @Bean
    public Client googleGenAiClient(
            @Value("${spring.ai.google.genai.api-key}") String apiKey,
            @Value("${civicassist.ai.timeout-ms:30000}") int timeoutMs
    ){
        return Client.builder()
                .apiKey(apiKey)
                .httpOptions(HttpOptions
                        .builder()
                        .timeout(timeoutMs)
                        .build()).build();
    }
}
