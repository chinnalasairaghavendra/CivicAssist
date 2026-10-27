package com.civicassist.triage;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GeminiTestController {

    private final ChatClient chatClient;

    public GeminiTestController(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @GetMapping("/api/auth/test-gemini")
    public String testGemini() {
        long start = System.currentTimeMillis();

        System.out.println("[TEST] Sending request to Gemini...");

        try {
            String response = chatClient.prompt()
                    .user("Reply with exactly one word: Hello")
                    .call()
                    .content();

            System.out.println("[TEST] Gemini responded in "
                    + (System.currentTimeMillis() - start) + " ms");

            System.out.println("[TEST] Response: " + response);

            return response;

        } catch (Exception e) {
            System.err.println("[TEST] Gemini request failed after "
                    + (System.currentTimeMillis() - start) + " ms");

            e.printStackTrace();
            throw e;
        }
    }
}