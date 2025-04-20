package discord_bot.controller;

import java.util.List;

import discord_bot.model.Messages;
import discord_bot.model.tasks.Album;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.FileUpload;

public class MessageManager {

	public static void sendMessage(SlashCommandInteractionEvent event, Messages message) {
		event.getHook().sendMessage(message.getMessage()).queue();
	}

	public static void sendMessage(SlashCommandInteractionEvent event, String message) {
		event.getHook().sendMessage(message).queue();
	}

	public static void showQueue(SlashCommandInteractionEvent event, Album album) {
		try {
			List<FileUpload> fileUpload = album.getFiles().stream().map(f -> FileUpload.fromData(f)).toList();

			event.getHook().sendFiles(fileUpload).queue();
			event.getHook().sendMessage(album.toString()).queue();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
