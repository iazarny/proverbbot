package com.az.proverbot;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class TextToSpeechService {

    private GoogleCredentials credentials = null;

    public InputStream synthesizeSpeech(String text) throws Exception {


        TextToSpeechSettings settings = TextToSpeechSettings.newBuilder().setCredentialsProvider(() -> getCredentials()).build();

        try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create(settings)) {
            //listAvailableVoices(textToSpeechClient);
            // Set the text input to be synthesized
            SynthesisInput input = SynthesisInput.newBuilder().setText(text).build();

            // Build the voice request, select the language code ("en-US") and the SSML
            // voice gender ("FEMALE" or "MALE")
            VoiceSelectionParams voice =
                    VoiceSelectionParams.newBuilder()
                            .setLanguageCode("uk-UA") // Use "uk-UA" for Ukrainian
                            .setSsmlGender(SsmlVoiceGender.MALE) // Or MALE, NEUTRAL
                            .setName("uk-UA-Chirp3-HD-Umbriel")
                            .build();

            // Select the type of audio file you want returned
            AudioConfig audioConfig =
                    AudioConfig.newBuilder().setAudioEncoding(AudioEncoding.MP3).build();

            // Perform the text-to-speech request on the text input with the selected voice parameters and audio file type
            SynthesizeSpeechResponse response =
                    textToSpeechClient.synthesizeSpeech(input, voice, audioConfig);

            // Get the audio contents as bytes
            ByteString audioContents = response.getAudioContent();

            return new ByteArrayInputStream(audioContents.toByteArray());
        }
    }

    public void listAvailableVoices(TextToSpeechClient textToSpeechClient) throws Exception {
        ListVoicesResponse response = textToSpeechClient.listVoices("uk-UA");
        for (Voice voice : response.getVoicesList()) {
            System.out.println("Name: " + voice.getName());
            System.out.println("Language Codes: " + voice.getLanguageCodesList());
            System.out.println("SSML Gender: " + voice.getSsmlGender());
            System.out.println("Natural Sample Rate Hertz: " + voice.getNaturalSampleRateHertz());
            System.out.println();
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