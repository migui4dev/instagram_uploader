package discord_bot.model.publications;

import java.io.File;
import java.time.ZonedDateTime;

import discord_bot.controller.DateManager;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class ScheduledPost extends Post implements Scheduled {
	private ZonedDateTime date;

	public ScheduledPost(SlashCommandInteractionEvent event, File attachmentFile, File coverFile, Attachment attachment,
			Attachment cover, String captions, ZonedDateTime date) {
		super(event, attachmentFile, coverFile, attachment, cover, captions, false);
		this.date = date;
	}

	@Override
	public ZonedDateTime getDate() {
		return date;
	}

	@Override
	public String toString() {
		final String postStr = super.toString();
		return String.format("%s Programado para: %s.", postStr, DateManager.formatDate(date));
	}

}
