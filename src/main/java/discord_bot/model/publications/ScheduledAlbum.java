package discord_bot.model.publications;

import java.time.ZonedDateTime;

import discord_bot.controller.DateManager;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class ScheduledAlbum extends Album implements Scheduled {
	private ZonedDateTime date;

	public ScheduledAlbum(SlashCommandInteractionEvent event, String captions, ZonedDateTime date) {
		super(event, captions);
		this.date = date;
	}

	@Override
	public ZonedDateTime getDate() {
		return date;
	}

	@Override
	public String toString() {
		final String filesStr = super.toString();

		return String.format("%s Programado para: %s.", filesStr, DateManager.formatDate(date));
	}

}
