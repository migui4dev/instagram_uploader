package discord_bot.model.tasks;

import java.io.File;
import java.time.LocalDateTime;

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
}
