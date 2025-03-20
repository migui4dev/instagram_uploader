package discord_bot.model;

import net.dv8tion.jda.api.entities.Message.Attachment;

public abstract class MyAttachment {
	private Attachment attachment;
	private String captions;

	public MyAttachment() {
	}

	public MyAttachment(Attachment attachment) {
		this.attachment = attachment;
	}

	public MyAttachment(Attachment attachment, String captions) {
		this.attachment = attachment;
		this.captions = captions;
	}

	public Attachment getAttachment() {
		return attachment;
	}

	public String getCaptions() {
		return captions;
	}

}
