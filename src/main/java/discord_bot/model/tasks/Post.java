package discord_bot.model.tasks;

import java.io.File;

import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class Post extends Publication {
	private File attachmentFile, coverFile;
	private Attachment attachment, cover;
	private boolean isStorie;

	public Post(SlashCommandInteractionEvent event, File attachmentFile, File coverFile, Attachment attachment,
			Attachment cover, String captions, boolean isStorie) {
		super(event, captions);
		this.attachmentFile = attachmentFile;
		this.coverFile = coverFile;
		this.attachment = attachment;
		this.cover = cover;
		this.isStorie = isStorie;
	}

	public File getAttachmentFile() {
		return attachmentFile;
	}

	public File getCoverFile() {
		return coverFile;
	}

	public Attachment getAttachment() {
		return attachment;
	}

	public Attachment getCover() {
		return cover;
	}

	public boolean isStorie() {
		return isStorie;
	}

	@Override
	public String toString() {
		return String.format("Id att: %s, cover id: %s, %s, Captions: '%s'.", attachmentFile.getName(),
				coverFile.getName(), isStorie ? "Storie" : "Post", captions);
	}
}
