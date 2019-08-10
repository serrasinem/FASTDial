package fastdial.interfaces.telegram;

import java.io.IOException;

import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import fastdial.FastLogger;
import fastdial.interfaces.service.Session;
import fastdial.modules.NLGLoader;

/**
 * Activates the Telegram bot by registering it
 * 
 * @author Serra Sinem Tekiroglu (tekiroglu@fbk.eu)
 */
public class RegisterBot {
	// FASTDial Telegram bot object
	public static FastDialTelegram fastbot = null;

	/**
	 * Starts up the telegram application. args are not utilized for any purposes.
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		ApiContextInitializer.init();
		NLGLoader.registerNLGFiles();
		try {
			FastLogger.setup();
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException("Problems with creating the log files");
		}

		// Instantiate Telegram Bots API
		TelegramBotsApi botsApi = new TelegramBotsApi();
		Session activelist = new Session();
		fastbot = new FastDialTelegram(activelist);

		try {
			botsApi.registerBot(fastbot);
		} catch (TelegramApiException e) {
			e.printStackTrace();
		}

	}
}