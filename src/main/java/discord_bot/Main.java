package discord_bot;

import static spark.Spark.get;
import static spark.Spark.port;

import java.time.LocalDateTime;

import discord_bot.controller.MyDateFormatter;
import discord_bot.model.Parameters;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import net.dv8tion.jda.internal.utils.JDALogger;

public class Main {
	private static final LocalDateTime DEPLOY_DATE = LocalDateTime.now();

	public static void main(String[] args) {
		port(8000);
		get("/", (req, res) -> String.format("Bot funcionando desde %s %n", MyDateFormatter.formatDate(DEPLOY_DATE)));

		System.out.printf("Versión %s %n", Bot.VERSION);

		try {
			String token = "";

			try {
				Dotenv dotenv = Dotenv.configure().directory("config/.env").load();
				token = dotenv.get("TOKEN");
			} catch (Exception e) {
				token = System.getenv("TOKEN");
			}

			JDALogger.setFallbackLoggerEnabled(false);

			JDA jda = JDABuilder.createDefault(token).addEventListeners(new Bot())
					.enableIntents(GatewayIntent.MESSAGE_CONTENT)
					.setActivity(Activity.customStatus(String.format("Versión %s", Bot.VERSION))).build().awaitReady();
			CommandListUpdateAction commands = jda.updateCommands();

			// POSTS
			commands.addCommands(Commands.slash("upload_post", "Post a video on your Instagram's account.")
					.addOption(OptionType.ATTACHMENT, Parameters.attachment.name(), "The file you want to post.", true)
					.addOption(OptionType.STRING, Parameters.captions.name(), "The captions that you set for the post.",
							true)
					.addOption(OptionType.ATTACHMENT, Parameters.cover.name(), "The cover you want on your post.",
							false));

			commands.addCommands(Commands
					.slash("upload_scheduled_post", "Schedule something on your Instagram's account.")
					.addOption(OptionType.ATTACHMENT, Parameters.attachment.name(), "The file you want to post.", true)
					.addOption(OptionType.STRING, Parameters.captions.name(), "The captions that you set for the post.",
							true)
					.addOption(OptionType.INTEGER, Parameters.day.name(),
							"The day you want to upload the video (default value is the current day).", false)
					.addOption(OptionType.INTEGER, Parameters.month.name(),
							"The month you want to upload the video (default value is the current month).", false)
					.addOption(OptionType.INTEGER, Parameters.year.name(),
							"The year you want to upload the video (default value is the current year).", false)
					.addOption(OptionType.INTEGER, Parameters.hour.name(),
							"The hour you want to upload the video (default value is the current hour).", false)
					.addOption(OptionType.INTEGER, Parameters.minute.name(),
							"The minute you want to upload the video (default value is the current minute).", false));

			// ALBUMS
			commands.addCommands(Commands.slash("upload_album",
					"Upload the queue of attachments you added with 'add_attachment'. Do NOT add videos on this.")
					.addOption(OptionType.STRING, Parameters.captions.name(), "The captions that you set for the post.",
							true));

			commands.addCommands(Commands
					.slash("upload_scheduled_album", "Schedule an album on your Instagram's account.")
					.addOption(OptionType.STRING, Parameters.captions.name(), "The captions that you set for the post.",
							true)
					.addOption(OptionType.ATTACHMENT, Parameters.cover.name(), "The cover you want on your post.",
							false)
					.addOption(OptionType.INTEGER, Parameters.day.name(),
							"The day you want to upload the video (default value is the current day).", false)
					.addOption(OptionType.INTEGER, Parameters.month.name(),
							"The month you want to upload the video (default value is the current month).", false)
					.addOption(OptionType.INTEGER, Parameters.year.name(),
							"The year you want to upload the video (default value is the current year).", false)
					.addOption(OptionType.INTEGER, Parameters.hour.name(),
							"The hour you want to upload the video (default value is the current hour).", false)
					.addOption(OptionType.INTEGER, Parameters.minute.name(),
							"The minute you want to upload the video (default value is the current minute).", false));

			// STORIES
			commands.addCommands(Commands.slash("upload_storie", "Post a video on your Instagram's daily stories.")
					.addOption(OptionType.ATTACHMENT, Parameters.attachment.name(), "The file you want for the storie.",
							true));

			// MISC.
			commands.addCommands(Commands.slash("login", "It is used to log in to an Instagram's account.")
					.addOption(OptionType.STRING, Parameters.username.name(), "The Instagram's account username.", true)
					.addOption(OptionType.STRING, Parameters.password.name(), "The Instagram's account password.", true)
					.addOption(OptionType.STRING, Parameters.verification_code.name(),
							"The Instagram's account verification code.", false));

			commands.addCommands(Commands.slash("logout", "It is used to log out of your Instagram account session."));

			commands.addCommands(
					Commands.slash("add_image", "Adds an attachment to queue of attachments for an album.").addOption(
							OptionType.ATTACHMENT, Parameters.attachment.name(), "Attachment to add to queue.", true));

			commands.addCommands(Commands.slash("clear_queue", "Clear the queue of attachments."));

			commands.addCommands(
					Commands.slash("show_queue", "If index not specified, it shows the first queue element.")
							.addOption(OptionType.INTEGER, Parameters.index.name(), "Index of element", false));

			commands.queue();

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
