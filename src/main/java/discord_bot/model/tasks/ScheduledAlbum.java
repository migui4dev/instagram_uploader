package discord_bot.model.tasks;

import java.time.LocalDateTime;

import discord_bot.controller.MyDateFormatter;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class ScheduledAlbum extends Album implements Scheduled {
	private LocalDateTime date;

	public ScheduledAlbum(SlashCommandInteractionEvent event, String captions, LocalDateTime date) {
		super(event, captions);
		this.date = date;
	}

	@Override
	public LocalDateTime getDate() {
		return date;
	}

	@Override
	public String toString() {
		final String filesStr = super.toString();

		return String.format("%s Programado para: %s.", filesStr, MyDateFormatter.formatDate(date));
	}

}
