package com.gptwe;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class OpenAiWeClient {
    private static final HttpClient HTTP = HttpClient.newHttpClient();

    private static String apiKey() {
        String key = System.getenv("OPENAI_API_KEY");
        if (key == null || key.isBlank()) {
            throw new IllegalStateException("Missing OPENAI_API_KEY");
        }
        return key;
    }

    public static String getWorldEditText(String userPrompt) throws IOException, InterruptedException {
        String prompt = """
                You are assisting in Minecraft with WorldEdit installed.
                Return ONLY WorldEdit commands, one per line.
                Every command must start with // .
                No explanations.
                Request: %s
                """.formatted(userPrompt);

        String body = """
                {
                  "model": "gpt-4o-mini",
                  "input": [
                    {
                      "role": "user",
                      "content": [
                        { "type": "input_text", "text": %s }
                      ]
                    }
                  ]
                }
                """.formatted(jsonString(prompt));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/responses"))
                .header("Authorization", "Bearer " + apiKey())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IOException("OpenAI HTTP " + response.statusCode() + ": " + response.body());
        }

        return extractText(response.body());
    }

    private static String jsonString(String s) {
        return "\"" + s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                + "\"";
    }

    private static String extractText(String json) {
        int idx = json.indexOf("\"text\"");
        if (idx < 0) return json;

        int colon = json.indexOf(':', idx);
        int firstQuote = json.indexOf('"', colon + 1);
        int secondQuote = findStringEnd(json, firstQuote + 1);

        if (firstQuote < 0 || secondQuote < 0) return json;

        return json.substring(firstQuote + 1, secondQuote)
                .replace("\\n", "\n")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }

    private static int findStringEnd(String s, int start) {
        boolean escaped = false;
        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);
            if (escaped) {
                escaped = false;
            } else if (c == '\\') {
                escaped = true;
            } else if (c == '"') {
                return i;
            }
        }
        return -1;
    }
}
