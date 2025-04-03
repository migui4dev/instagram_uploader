package discord_bot.controller;

import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.IGClient.Builder.LoginHandler;
import com.github.instagram4j.instagram4j.exceptions.IGLoginException;
import com.github.instagram4j.instagram4j.utils.IGChallengeUtils;

import discord_bot.model.Messages;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class SessionManager {
	private static IGClient client = null;

	public static boolean login(SlashCommandInteractionEvent event, String username, String password,
			String verificationCode) {
		LoginHandler challengeHandler = (client, response) -> IGChallengeUtils.resolveChallenge(client, response,
				() -> verificationCode);
		try {
			if (verificationCode == null) {
				client = IGClient.builder().username(username).password(password).login();
			} else {
				client = IGClient.builder().username(username).password(password).onChallenge(challengeHandler).login();
			}

			MessageManager.sendMessage(event, Messages.SUCCESS_LOGIN);
		} catch (IGLoginException e) {
			MessageManager.sendMessage(event, Messages.FAILED_LOGIN);
			e.printStackTrace();
		}

		return client.isLoggedIn();
	}

	public static void logout(SlashCommandInteractionEvent event, boolean hasScheduledTask) {
		if (!hasScheduledTask) {
			System.out.println("[!] Hay una tarea programada, no puedes cerrar sesión.");
			MessageManager.sendMessage(event, "Hay una tarea programada, no puedes cerrar sesión.");
			return;
		}

		client = null;
		MessageManager.sendMessage(event, Messages.SUCCESS_LOGOUT);
	}

	public static IGClient getClient() {
		return client;
	}
}
