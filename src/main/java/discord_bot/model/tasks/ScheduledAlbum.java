package discord_bot.model.tasks;

import java.time.LocalDateTime;

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

}
