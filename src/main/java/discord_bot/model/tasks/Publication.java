package discord_bot.model.tasks;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public abstract class Publication {
	protected SlashCommandInteractionEvent event;
	protected String captions;

	public Publication(SlashCommandInteractionEvent event, String captions) {
		this.event = event;
		this.captions = captions;
	}

	public SlashCommandInteractionEvent getEvent() {
		return event;
	}

	public String getCaptions() {
		return captions;
	}

	public void setCaptions(String captions) {
		this.captions = captions;
	}

}
