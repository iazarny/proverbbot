package com.az.proverbot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
@EnableScheduling
public class ProverbotApplication {


	@Value("${telegram.bot.token}")
	private String botToken;

	@Value("${telegram.bot.username}")
	private String botUsername;

	public static void main(String[] args) {
		SpringApplication.run(ProverbotApplication.class, args);
	}

	@Bean
	public TelegramBotsApi telegramBotsApi(ProverbBot proverbBot) throws TelegramApiException {
		TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
		api.registerBot(proverbBot);
		return api;
	}

	@Bean
	public ProverbBot proverbBot(GeminiProverbService geminiProverbService, TextToSpeechService textToSpeechService) {
		return new ProverbBot(botToken, botUsername, geminiProverbService, textToSpeechService);
	}

}
