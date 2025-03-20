package discord_bot.model;

import net.dv8tion.jda.api.entities.Message.Attachment;

public class Video extends MyAttachment {
	private Attachment cover;

	public Video() {
		super();
	}

	public Video(Attachment attachment, Attachment cover) {
		super(attachment);
		this.cover = cover;
	}

	public Video(Attachment attachment, String captions) {
		super(attachment, captions);
	}

	public Video(Attachment attachment, Attachment cover, String captions) {
		super(attachment, captions);
		this.cover = cover;
	}

	public Attachment getCover() {
		return cover;
	}

	public boolean hasCover() {
		return cover != null;
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
