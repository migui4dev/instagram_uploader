package discord_bot.model.tasks;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import discord_bot.controller.FileManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Album extends Publication {
	private final List<File> files;

	public Album(SlashCommandInteractionEvent event, String captions) {
		super(event, captions);
		this.files = new ArrayList<>();
	}

	public List<File> getFiles() {
		return files;
	}

	public void addFile(File file) {
		this.files.add(file);
	}

	public void addAllFiles(List<File> files) {
		this.files.addAll(files);
	}

	public void clearFiles() {
		for (File f : files) {
			FileManager.deleteFile(f);
		}

		this.files.clear();
	}

	@Override
	public String toString() {
		final String filesStr = String.join(",", files.stream().map(t -> t.getName()).toList());

		return String.format("Álbum: [%s]. Captions: '%s'.", filesStr, captions);
	}

}
