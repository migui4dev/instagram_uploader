package discord_bot.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import discord_bot.model.Messages;
import discord_bot.model.publications.Album;
import discord_bot.model.publications.Post;
import discord_bot.model.publications.Publication;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.FileUpload;

public class MessageManager {

	public static void sendMessage(SlashCommandInteractionEvent event, Messages message) {
		event.getHook().sendMessage(message.getMessage()).queue();
	}

	public static void sendMessage(SlashCommandInteractionEvent event, String message) {
		event.getHook().sendMessage(message).queue();
	}

	public static void sendFiles(SlashCommandInteractionEvent event, Publication p) {
		Album album = null;
		Post post = null;
		final List<File> files = new ArrayList<>();

		if (p instanceof Album) {
			album = (Album) p;
			files.addAll(album.getFiles());
		} else {
			post = (Post) p;
			files.add(post.getAttachmentFile());
			files.add(post.getCoverFile());
		}

		try {
			final List<FileUpload> fileUpload = files.stream().map(FileUpload::fromData).toList();

			String msg;

			if (album != null) {
				msg = album.toString();
			} else {
				msg = post.toString();
			}

			event.getHook().sendFiles(fileUpload).queue();
			event.getHook().sendMessage(msg).queue();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
