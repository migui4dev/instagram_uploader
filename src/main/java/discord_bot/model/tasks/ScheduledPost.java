package discord_bot.model.tasks;

import java.io.File;
import java.time.LocalDateTime;

import discord_bot.controller.MyDateFormatter;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class ScheduledPost extends Post implements Scheduled {
	private LocalDateTime date;

	public ScheduledPost(SlashCommandInteractionEvent event, File attachmentFile, File coverFile, Attachment attachment,
			Attachment cover, String captions, LocalDateTime date) {
		super(event, attachmentFile, coverFile, attachment, cover, captions, false);
		this.date = date;
	}

	@Override
	public LocalDateTime getDate() {
		return date;
	}

	@Override
	public String toString() {
		final String postStr = super.toString();
		return String.format("%s (%s)", postStr, MyDateFormatter.formatDate(date));
	}
}
