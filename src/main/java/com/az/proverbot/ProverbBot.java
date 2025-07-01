package com.az.proverbot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ProverbBot extends TelegramLongPollingBot {

    private final String botToken;
    private final String botUsername;
    private final GeminiProverbService geminiProverbService;
    private final TextToSpeechService textToSpeechService;

    public ProverbBot(String botToken, String botUsername, GeminiProverbService geminiProverbService, TextToSpeechService textToSpeechService) {
        super(botToken);
        this.botToken = botToken;
        this.botUsername = botUsername;
        this.geminiProverbService = geminiProverbService;
        this.textToSpeechService = textToSpeechService;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (!messageText.startsWith("/proverb")) {
                String theme = messageText.replace("/proverb", "").trim();
                if (theme.isEmpty()) {
                    sendMessage(chatId, "Скажи мені, про що ти хочеш, щоб я тобі розповів анекдот.");
                    return;
                }

                try {
                    // 1. Get proverb from Gemini
                    String proverb = geminiProverbService.getProverbByTheme(theme);
                    if (proverb == null || proverb.isEmpty()) {
                        sendMessage(chatId, "Щось не можу знайти на тему: " + theme);
                        return;
                    }

                    System.out.println(proverb);
                    System.out.println();

                    // Send text message first
                    sendMessage(chatId, "Слухайте:\n\n" + proverb);

                    // 2. Convert text to speech
                    InputStream audioStream = textToSpeechService.synthesizeSpeech(proverb);

                    // 3. Send audio to Telegram
                    sendAudio(chatId, audioStream, "proverb.mp3");

                } catch (Exception e) {
                    e.printStackTrace();
                    sendMessage(chatId, "An error occurred while fetching or speaking the proverb. Please try again later.");
                }
            } else {
                sendMessage(chatId, "Привіт, а анекдот бот. Щоб скористатись набері /proverb [theme] (наприклад /proverb про котів).");
            }
        }
    }

    private void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendAudio(long chatId, InputStream audioStream, String fileName) {
        SendAudio sendAudio = new SendAudio();
        sendAudio.setChatId(chatId);
        sendAudio.setAudio(new InputFile(audioStream, fileName));
        try {
            execute(sendAudio);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        } finally {
            try {
                if (audioStream != null) {
                    audioStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}