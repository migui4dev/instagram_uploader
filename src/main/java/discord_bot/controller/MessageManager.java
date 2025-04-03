package discord_bot.controller;

import discord_bot.model.Messages;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class MessageManager {

	public static void sendMessage(SlashCommandInteractionEvent event, Messages message) {
		event.getHook().sendMessage(message.getMessage()).queue();
	}

	public static void sendMessage(SlashCommandInteractionEvent event, String message) {
		event.getHook().sendMessage(message).queue();
	}
}
