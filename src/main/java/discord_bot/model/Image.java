package discord_bot.model;

import net.dv8tion.jda.api.entities.Message.Attachment;

public class Image extends MyAttachment {

	public Image() {
		super();
	}

	public Image(Attachment attachment) {
		super(attachment);
	}

	public Image(Attachment attachment, String captions) {
		super(attachment, captions);
	}

	@Override
	public Attachment getAttachment() {
		return super.getAttachment();
	}

	@Override
	public String getCaptions() {
		return super.getCaptions();
	}
}
