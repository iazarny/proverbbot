package com.az.proverbot;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class GeminiProverbService {

    @Value("${gemini.project.id}")
    private String projectId;

    @Value("${gemini.location}")
    private String location;

    @Value("${gemini.model.name}")
    private String modelName; // e.g., "gemini-pro"

    private GoogleCredentials credentials = null;

    public String getProverbByTheme(String theme) {
        try (VertexAI vertexAI = new VertexAI.Builder()
                .setProjectId(projectId)
                .setLocation(location)
                .setCredentials(getCredentials())
                .build()) {

            GenerativeModel model = new GenerativeModel(modelName, vertexAI);

            // Crafting a clear prompt for Gemini
            //String prompt = "Give me a famous proverb related to the theme of \"" + theme + "\". Only provide the proverb text, nothing else. If you cannot find one, just say 'No proverb found.'";
            String prompt = "Розкажи мені короткий анекдот на тему " + theme + ". Відповідь має бути лише чистим текстом, без будь-яких спеціальних символів, розмітки чи додаткових фраз. Використовуй стандартну українську мову. Якщо нема то скажи що тебе треба трохи пивасика та віскаря щоб краще розуміти";

            GenerateContentResponse response = model.generateContent(prompt);
            String textResponse = ResponseHandler.getText(response);

            // Basic extraction (Gemini's response should ideally be just the proverb)
            // You might need more robust JSON parsing if Gemini returns structured JSON
            // For now, assuming it returns plain text proverb
            if (textResponse != null && !textResponse.trim().equals("Та ну ! Щось я замахався. Давай краще пивасика та віскаря")) {
                return textResponse.trim();
            } else {
                return null; // No proverb found or error
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



    private synchronized GoogleCredentials getCredentials() throws IOException {
        if (credentials == null) {
            credentials = GoogleCredentials.fromStream(this.getClass().getResourceAsStream("/gromada-ms-81f3f24ec554.json"))
                    .createScoped(
                    "https://www.googleapis.com/auth/cloud-platform", // Broad scope, includes AI Platform and TTS
                    "https://www.googleapis.com/auth/generative-language.tuning" // If you use specific GenAI features

            );
        }
        // Load your service account key file
        return credentials;
    }
}