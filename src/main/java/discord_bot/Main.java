package discord_bot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.internal.utils.JDALogger;

public class Main {

	public static void main(String[] args) {
		System.out.println(Bot.getVersionMessage());

		try {
			String token = System.getenv("TOKEN");
			JDALogger.setFallbackLoggerEnabled(false);

			JDA jda = JDABuilder.createDefault(token).addEventListeners(new Bot())
					.enableIntents(GatewayIntent.MESSAGE_CONTENT)
					.setActivity(Activity.customStatus(Bot.getVersionMessage())).build().awaitReady();
			CommandListUpdateAction commands = jda.updateCommands();

			// POSTS
			commands.addCommands(Commands.slash("upload_post", "Post a video on your Instagram's account.")
					.addOption(OptionType.ATTACHMENT, "attachment", "The file you want to post.", true)
					.addOption(OptionType.STRING, "captions", "The captions that you set for the post.", true)
					.addOption(OptionType.ATTACHMENT, "cover", "The cover you want on your post.", false));

			commands.addCommands(Commands
					.slash("upload_scheduled_post", "Schedule something on your Instagram's account.")
					.addOption(OptionType.ATTACHMENT, "attachment", "The file you want to post.", true)
					.addOption(OptionType.STRING, "captions", "The captions that you set for the post.", true)
					.addOption(OptionType.INTEGER, "day",
							"The day you want to upload the video (default value is the current day).", false)
					.addOption(OptionType.INTEGER, "month",
							"The month you want to upload the video (default value is the current month).", false)
					.addOption(OptionType.INTEGER, "year",
							"The year you want to upload the video (default value is the current year).", false)
					.addOption(OptionType.INTEGER, "hour",
							"The hour you want to upload the video (default value is the current hour).", false)
					.addOption(OptionType.INTEGER, "minute",
							"The minute you want to upload the video (default value is the current minute).", false));

			// ALBUMS
			commands.addCommands(Commands.slash("upload_album",
					"Upload the queue of attachments you added with 'add_attachment'. Do NOT add videos on this.")
					.addOption(OptionType.STRING, "captions", "The captions that you set for the post.", true));

			commands.addCommands(Commands
					.slash("upload_scheduled_album", "Schedule an album on your Instagram's account.")
					.addOption(OptionType.STRING, "captions", "The captions that you set for the post.", true)
					.addOption(OptionType.ATTACHMENT, "cover", "The cover you want on your post.", false)
					.addOption(OptionType.INTEGER, "day",
							"The day you want to upload the video (default value is the current day).", false)
					.addOption(OptionType.INTEGER, "month",
							"The month you want to upload the video (default value is the current month).", false)
					.addOption(OptionType.INTEGER, "year",
							"The year you want to upload the video (default value is the current year).", false)
					.addOption(OptionType.INTEGER, "hour",
							"The hour you want to upload the video (default value is the current hour).", false)
					.addOption(OptionType.INTEGER, "minute",
							"The minute you want to upload the video (default value is the current minute).", false));

			// STORIES
			commands.addCommands(Commands.slash("upload_storie", "Post a video on your Instagram's daily stories.")
					.addOption(OptionType.ATTACHMENT, "attachment", "The file you want for the storie.", true));

			// MISC.
			commands.addCommands(Commands.slash("login", "It is used to log in to an Instagram's account.")
					.addOption(OptionType.STRING, "username", "The Instagram's account username.", true)
					.addOption(OptionType.STRING, "password", "The Instagram's account password.", true)
					.addOption(OptionType.STRING, "verification_code", "The Instagram's account verification code.",
							false));

			commands.addCommands(Commands.slash("logout", "It is used to log out of your Instagram account session."));

			commands.addCommands(Commands.slash("add_image", "Adds an attachment to queue of attachments for an album.")
					.addOption(OptionType.ATTACHMENT, "attachment", "Attachment to add to queue.", true));

			commands.addCommands(Commands.slash("clear_queue", "Clear the queue of attachments."));

			commands.queue();

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
