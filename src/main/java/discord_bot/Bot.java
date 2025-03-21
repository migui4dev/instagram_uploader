package discord_bot;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.IGClient.Builder.LoginHandler;
import com.github.instagram4j.instagram4j.actions.timeline.TimelineAction.SidecarInfo;
import com.github.instagram4j.instagram4j.actions.timeline.TimelineAction.SidecarPhoto;
import com.github.instagram4j.instagram4j.actions.timeline.TimelineAction.SidecarVideo;
import com.github.instagram4j.instagram4j.exceptions.IGLoginException;
import com.github.instagram4j.instagram4j.utils.IGChallengeUtils;

import discord_bot.model.Image;
import discord_bot.model.MyAttachment;
import discord_bot.model.Video;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class Bot extends ListenerAdapter {
	private Member memberUsingBot;
	private IGClient client;

	private final ArrayList<MyAttachment> filesQueue = new ArrayList<>();
	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		if (memberUsingBot != null && !memberUsingBot.equals(event.getMember())) {
			sendMessage(event, String.format("You cannot use the bot, because %s is already using it.",
					memberUsingBot.getNickname()));
			return;
		}

		event.deferReply(true).queue();
		this.memberUsingBot = event.getMember();

		switch (event.getName().toLowerCase()) {
		case "login" -> {
			if (client != null && client.isLoggedIn()) {
				sendMessage(event, "You are already logged-in.");
				return;
			}

			String username, password, verificationCode;

			username = event.getOption("username").getAsString().strip();
			password = event.getOption("password").getAsString().strip();

			try {
				verificationCode = event.getOption("verification_code").getAsString().strip();
			} catch (NullPointerException e) {
				verificationCode = null;
			}

			login(event, username, password, verificationCode);
		}
		case "logout" -> {
			logout(event);
		}
		case "add_image" -> {
			Attachment attachment = event.getOption("attachment").getAsAttachment();

			if (!attachment.isImage()) {
				sendMessage(event, "That file extension does not have support.");
				return;
			}

			Image image = new Image(attachment);
			filesQueue.add(image);
			sendMessage(event, "Image added to queue successfully.");
		}

		case "clear_queue" -> {
			filesQueue.clear();
			sendMessage(event, "Attachment queue cleared successfully.");
		}
		case "upload_post", "upload_scheduled_post" -> {
			LocalDateTime now = LocalDateTime.now();
			Attachment attachment = event.getOption("attachment").getAsAttachment();
			String captions = event.getOption("captions").getAsString();

			Optional<OptionMapping> cover = Optional.ofNullable(event.getOption("cover"));
			Attachment coverValue = cover.isPresent() ? cover.get().getAsAttachment() : null;

			Optional<OptionMapping> day = Optional.ofNullable(event.getOption("day"));
			Optional<OptionMapping> month = Optional.ofNullable(event.getOption("month"));
			Optional<OptionMapping> year = Optional.ofNullable(event.getOption("year"));

			Optional<OptionMapping> hour = Optional.ofNullable(event.getOption("hour"));
			Optional<OptionMapping> minute = Optional.ofNullable(event.getOption("minute"));

			int dayValue = day.isPresent() ? day.get().getAsInt() : now.getDayOfMonth();
			int monthValue = month.isPresent() ? month.get().getAsInt() : now.getMonthValue();
			int yearValue = year.isPresent() ? year.get().getAsInt() : now.getYear();

			int hourValue = hour.isPresent() ? hour.get().getAsInt() : now.getHour();
			int minuteValue = minute.isPresent() ? minute.get().getAsInt() : now.getMinute();

			try {
				LocalDateTime dateToSchedule = LocalDateTime.of(yearValue, monthValue, dayValue, hourValue,
						minuteValue);

				if (dateToSchedule.isAfter(now)) {
					sendMessage(event, String.format("Scheduled post for: %s.", formatDate(dateToSchedule)));
				}

				scheduler.schedule(() -> {
					if (attachment.isImage()) {
						Image image = new Image(attachment, captions);
						uploadImage(event, image, false);
					} else {
						Video video = new Video(attachment, coverValue, captions);
						uploadVideo(event, video, false);
					}
				}, Duration.between(now, dateToSchedule).toSeconds(), TimeUnit.SECONDS);

			} catch (DateTimeException e) {
				sendMessage(event, "Bad formed date.");
			}

		}
		case "upload_storie" -> {
			Attachment attachment = event.getOption("attachment").getAsAttachment();

			Optional<OptionMapping> cover = Optional.ofNullable(event.getOption("cover"));
			Attachment coverValue = cover.isPresent() ? cover.get().getAsAttachment() : null;

			if (attachment.isImage()) {
				Image image = new Image(attachment);
				uploadImage(event, image, true);
			} else {
				Video video = new Video(attachment, coverValue);
				uploadVideo(event, video, true);
			}
		}
		case "upload_album" -> {
			String captions = event.getOption("captions").getAsString();
			uploadAlbum(event, captions);
		}

		}
	}

	private String formatDate(LocalDateTime date) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm");
		return date.format(dtf);
	}

	private void login(SlashCommandInteractionEvent event, String username, String password, String verificationCode) {
		LoginHandler twoFactorHandler = (client, response) -> {
			return IGChallengeUtils.resolveTwoFactor(client, response, () -> verificationCode);
		};

		try {
			if (verificationCode == null) {
				client = IGClient.builder().username(username).password(password).login();
			} else {
				client = IGClient.builder().username(username).password(password).onTwoFactor(twoFactorHandler).login();
			}

			sendMessage(event, "Logged-in successfully.");
		} catch (IGLoginException e) {
			sendMessage(event, String.format("I could not log-in with %s %n", username));
		}

	}

	private void logout(SlashCommandInteractionEvent event) {
		client = null;
		memberUsingBot = null;
		sendMessage(event, "Logged-out successfully.");
	}

	private void uploadFile(SlashCommandInteractionEvent event, File attachment, File cover, String captions,
			boolean isStorie, boolean isVideo) {
		if (isVideo) {
			if (isStorie) {
				client.actions().story().uploadVideo(attachment, cover).thenAccept(t -> {
					sendMessage(event, "Storie (video) uploaded successfully.");
				}).exceptionally(t -> {
					sendMessage(event, String.format(
							"An error has ocurred: %s. (Probably the resolution is not allowed).", t.getMessage()));
					t.printStackTrace();
					return null;
				}).join();
			} else {
				client.actions().timeline().uploadVideo(attachment, cover, captions).thenAccept(t -> {
					sendMessage(event, "Post (video) uploaded successfully.");
				}).exceptionally(t -> {
					sendMessage(event, String.format(
							"An error has ocurred: %s. (Probably the resolution is not allowed).", t.getMessage()));
					t.printStackTrace();
					return null;
				}).join();
			}
		} else {
			if (isStorie) {
				client.actions().story().uploadPhoto(attachment).thenAccept(t -> {
					sendMessage(event, "Storie (image) uploaded successfully.");
				}).exceptionally(t -> {
					sendMessage(event, String.format(
							"An error has ocurred: %s. (Probably the resolution is not allowed).", t.getMessage()));
					t.printStackTrace();
					return null;
				}).join();
			} else {
				client.actions().timeline().uploadPhoto(attachment, captions).thenAccept(t -> {
					sendMessage(event, "Post (image) uploaded successfully.");
				}).exceptionally(t -> {
					sendMessage(event, String.format(
							"An error has ocurred: %s. (Probably the resolution is not allowed).", t.getMessage()));
					t.printStackTrace();
					return null;
				}).join();
			}
		}

	}

	private void uploadImage(SlashCommandInteractionEvent event, Image image, boolean isStorie) {
		if (!image.getAttachment().isImage()) {
			sendMessage(event, "Not supported attachment's extension.");
			return;
		}

		File imageFile = processAttachment(event, image);

		if (imageFile == null) {
			return;
		}

		uploadFile(event, imageFile, null, image.getCaptions(), isStorie, false);

		if (imageFile.exists()) {
			imageFile.delete();
		}

	}

	private void uploadVideo(SlashCommandInteractionEvent event, Video video, boolean isStorie) {
		if (!video.getAttachment().isVideo()) {
			sendMessage(event, "Not supported attachment's extension.");
			return;
		}

		File videoFile = processAttachment(event, video);
		File coverFile = processAttachment(event, new Image(video.getCover()));

		if (videoFile == null) {
			return;
		} else if (coverFile == null) {
			return;
		}

		uploadFile(event, videoFile, coverFile, video.getCaptions(), isStorie, true);

		if (videoFile.exists()) {
			videoFile.delete();
		}

		if (coverFile.exists()) {
			coverFile.delete();
		}

	}

	private void uploadAlbum(SlashCommandInteractionEvent event, String captions) {
		ArrayList<SidecarInfo> album = new ArrayList<>();

		int contImg = 1;
		int contVideo = 1;

		for (MyAttachment attachment : filesQueue) {
			if (attachment.getAttachment().isImage()) {
				Image image = (Image) attachment;
				File imageFile = processAttachment(event, image);
				System.out.printf("Imagen: %d %n", contImg++);

				if (imageFile == null) {
					return;
				}

				album.add(SidecarPhoto.from(imageFile));
			} else {
				Video video = (Video) attachment;
				File videoFile = processAttachment(event, video);
				File coverFile = processAttachment(event, new Image(video.getAttachment()));
				System.out.printf("Vídeo: %d %n", contVideo++);

				if (videoFile == null) {
					return;
				} else if (coverFile == null) {
					return;
				}

				album.add(SidecarVideo.from(videoFile, coverFile));
			}
		}

		if (album.size() <= 1) {
			sendMessage(event, "You only can upload an album with 2 or more attachments.");
			return;
		}

		client.actions().timeline().uploadAlbum(album, captions).thenAccept(t -> {
			sendMessage(event, "Album uploaded successfully.");
		}).exceptionally((t) -> {
			sendMessage(event, String.format("An error has ocurred: %s", t.getMessage()));
			t.printStackTrace();
			return null;
		}).join();

		for (MyAttachment attachment : filesQueue) {
			if (attachment.getAttachment().isImage()) {
				File f = new File(attachment.getAttachment().getId());

				if (f.exists()) {
					f.delete();
				}
			} else if (attachment.getAttachment().isVideo()) {
				Video v = (Video) attachment;

				File videoFile = new File(v.getAttachment().getId());
				File coverFile = new File(v.getCover().getId());

				if (videoFile.exists()) {
					videoFile.delete();
				}

				if (coverFile.exists()) {
					coverFile.delete();
				}
			}
		}

		filesQueue.clear();

	}

	private File processAttachment(SlashCommandInteractionEvent event, MyAttachment att) {
		if (att == null || att.getAttachment() == null) {
			return null;
		}

		File f = new File(att.getAttachment().getId());

		try (DataOutputStream dosAtt = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f)))) {
			URL urlAtt = new URI(att.getAttachment().getUrl()).toURL();
			InputStream is = urlAtt.openStream();

			dosAtt.write(is.readAllBytes());
		} catch (MalformedURLException | URISyntaxException e) {
			e.printStackTrace();
			sendMessage(event, "Something went wrong...");
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			sendMessage(event, "File error.");
			return null;
		}

		return f;
	}

	private void sendMessage(SlashCommandInteractionEvent event, String message) {
		event.getHook().sendMessage(message).queue();
	}

}
