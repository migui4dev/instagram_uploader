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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.github.instagram4j.instagram4j.IGClient;
import com.github.instagram4j.instagram4j.IGClient.Builder.LoginHandler;
import com.github.instagram4j.instagram4j.actions.timeline.TimelineAction.SidecarInfo;
import com.github.instagram4j.instagram4j.actions.timeline.TimelineAction.SidecarPhoto;
import com.github.instagram4j.instagram4j.exceptions.IGLoginException;
import com.github.instagram4j.instagram4j.utils.IGChallengeUtils;

import discord_bot.model.Messages;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class Bot extends ListenerAdapter {
	private static final String VERSION = "1.3 BETA";

	private final boolean debug = true;

	private final List<File> fileQueue = new ArrayList<>();
	private final Map<String, Object> parameters = new ConcurrentHashMap<>();

	private Thread thread;
	private boolean scheduledTask = false;
	private Member memberUsingBot;
	private IGClient client;

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		if (memberUsingBot != null && !memberUsingBot.equals(event.getMember())) {
			sendMessage(event, Messages.SOMEONE_USING_BOT.getMessage());
			return;
		}

		this.memberUsingBot = event.getMember();

		if (debug && !memberUsingBot.getId().equals("471697821947396096")) {
			return;
		}

		event.deferReply(true).queue();

		// Desde aquí, únicamente se recogerán los parámetros y se llamarán a las
		// funciones correspondientes.
		switch (event.getName().toLowerCase()) {
		case "login" -> {
			if (client != null && client.isLoggedIn()) {
				sendMessage(event, Messages.ALREADY_LOGGED.getMessage());
				return;
			}

			try {
				processParameters(event.getOptions());
			} catch (Exception e) {
				e.printStackTrace();
				sendMessage(event, Messages.GENERIC.getMessage());
				return;
			}

			login(event);
		}
		case "logout" -> {
			logout(event);
		}
		case "add_image" -> {
			Attachment attachment = event.getOption("attachment").getAsAttachment();
			addImageToQueue(event, attachment);
		}
		case "clear_queue" -> {
			clearQueue(event);
		}
		case "upload_post" -> {
			try {
				processParameters(event.getOptions());
			} catch (Exception e) {
				e.printStackTrace();
				sendMessage(event, e.getMessage());
				System.out.println(e.getMessage());
				return;
			}

			Attachment attachment = (Attachment) parameters.get("attachment");
			Attachment cover = parameters.containsKey("cover") ? (Attachment) parameters.get("cover") : attachment;

			parameters.put("is_storie", false);
			parameters.put("is_video", attachment.isVideo());

			File attFile = saveFile(event, attachment);
			File coverFile = saveFile(event, cover);

			parameters.put("attachment", attFile);
			parameters.put("cover", coverFile);

			uploadFile(event);
		}
		case "upload_storie" -> {
			try {
				processParameters(event.getOptions());
				Attachment att = (Attachment) parameters.get("attachment");
				parameters.put("is_storie", true);
				parameters.put("is_video", att.isVideo());
				File attFile = saveFile(event, att);
				parameters.put("attachment", attFile);
			} catch (Exception e) {
				e.printStackTrace();
				sendMessage(event, e.getMessage());
				return;
			}

			uploadFile(event);
		}
		case "upload_album" -> {
			String captions = event.getOption("captions").getAsString();
			uploadAlbum(event, captions);
			clearQueue(event);
		}
		case "upload_scheduled_post" -> {
			try {
				processParameters(event.getOptions());

				Attachment att = (Attachment) parameters.get("attachment");

				parameters.put("is_video", att.isVideo());
				parameters.put("is_storie", false);

				File f = saveFile(event, att);
				parameters.put("attachment", f);

				uploadScheduled(event);
			} catch (Exception e) {
				e.printStackTrace();
				sendMessage(event, e.getMessage());
			}
		}
		case "upload_scheduled_album" -> {
			try {
				// No se llama al método "saveFile()", porque al añadir a la cola las imágenes,
				// ya se llama a ese método.
				processParameters(event.getOptions());

				parameters.put("is_video", false);
				parameters.put("is_storie", false);

				uploadScheduled(event);
			} catch (Exception e) {
				e.printStackTrace();
				sendMessage(event, e.getMessage());
			}
		}

		}
	}

	public static String getVersionMessage() {
		return String.format("Versión %s", VERSION);
	}

	private LocalDateTime wellFormedDate(int year, int month, int day, int hour, int minute) {
		try {
			LocalDateTime ldt = LocalDateTime.of(year, month, day, hour, minute);
			return ldt;
		} catch (DateTimeException e) {
			return null;
		}
	}

	private static String formatLocalDateTime(LocalDateTime date) {
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
		return date.format(dtf);
	}

	private void processParameters(List<OptionMapping> options) throws Exception {
		parameters.clear();

		for (OptionMapping o : options) {
			Object value;

			switch (o.getType()) {
			case INTEGER -> {
				value = o.getAsInt();
			}
			case STRING -> {
				value = o.getAsString();
			}
			case ATTACHMENT -> {
				value = o.getAsAttachment();
			}
			default -> {
				throw new Exception("Invalid argument type.");
			}
			}

			parameters.put(o.getName(), value);
		}

		parameters.put("is_album", !parameters.containsKey("attachment"));
	}

	private String processCaptions(String captions) {
		if (!captions.contains("%s")) {
			return captions;
		}

		return captions.replaceAll("%s", "\r\n");
	}

	private void login(SlashCommandInteractionEvent event) {
		String username = (String) parameters.get("username");
		String password = (String) parameters.get("password");
		String verificationCode = parameters.containsKey("verification_code")
				? (String) parameters.get("verification_code")
				: null;

		LoginHandler challengeHandler = (client, response) -> IGChallengeUtils.resolveChallenge(client, response,
				() -> verificationCode != null ? verificationCode : "");

		try {
			if (verificationCode == null) {
				client = IGClient.builder().username(username).password(password).login();
			} else {
				client = IGClient.builder().username(username).password(password).onChallenge(challengeHandler).login();
			}
			sendMessage(event, Messages.SUCCESS_LOGIN.getMessage());
		} catch (IGLoginException e) {
			sendMessage(event, Messages.FAILED_LOGIN.getMessage());
			e.printStackTrace();
		}

	}

	private void logout(SlashCommandInteractionEvent event) {
		if (scheduledTask) {
			sendMessage(event, "There is a scheduled task. You cannot logout.");
			return;
		}

		client = null;
		memberUsingBot = null;
		sendMessage(event, Messages.SUCCESS_LOGOUT.getMessage());
	}

	private void uploadFile(SlashCommandInteractionEvent event) {
		try {
			if (client == null || !client.isLoggedIn()) {
				sendMessage(event, "No hay una sesión iniciada.");
				return;
			}

			File attachment = (File) parameters.get("attachment");
			File cover = parameters.get("cover") != null ? (File) parameters.get("cover_file") : attachment;

			String captions = parameters.containsKey("captions") ? parameters.get("captions").toString() : "";
			captions = String.format("%s", processCaptions(captions));

			boolean isStorie = (boolean) parameters.get("is_storie");
			boolean isVideo = (boolean) parameters.get("is_video");

			if (isVideo) {
				if (isStorie) {
					client.actions().story().uploadVideo(attachment, cover).thenAccept(t -> {
						sendMessage(event, Messages.VIDEO_STORIE_UPLOADED.getMessage());
					}).exceptionally(t -> {
						sendMessage(event,
								String.format(
										"Ocurrió un error: %s.%n(Probablemente sea una mala resolución de la imagen).",
										t.getMessage()));
						t.printStackTrace();
						return null;
					}).join();
				} else {
					client.actions().timeline().uploadVideo(attachment, cover, captions).thenAccept(t -> {
						sendMessage(event, Messages.VIDEO_POST_UPLOADED.getMessage());
					}).exceptionally(t -> {
						sendMessage(event,
								String.format(
										"Ocurrió un error: %s.%n(Probablemente sea una mala resolución de la imagen).",
										t.getMessage()));
						t.printStackTrace();
						return null;
					}).join();
				}
			} else {
				if (isStorie) {
					client.actions().story().uploadPhoto(attachment).thenAccept(t -> {
						sendMessage(event, Messages.IMAGE_STORIE_UPLOADED.getMessage());
					}).exceptionally(t -> {
						sendMessage(event,
								String.format(
										"Ocurrió un error: %s.%n(Probablemente sea una mala resolución de la imagen).",
										t.getMessage()));
						t.printStackTrace();
						return null;
					}).join();
				} else {
					client.actions().timeline().uploadPhoto(attachment, captions).thenAccept(t -> {
						sendMessage(event, Messages.IMAGE_POST_UPLOADED.getMessage());
					}).exceptionally(t -> {
						sendMessage(event,
								String.format(
										"Ocurrió un error: %s.%n(Probablemente sea una mala resolución de la imagen).",
										t.getMessage()));
						t.printStackTrace();
						return null;
					}).join();
				}
			}

			if (attachment.exists()) {
				attachment.delete();
			}

			if (cover.exists()) {
				cover.delete();
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

	private void uploadAlbum(SlashCommandInteractionEvent event, String captions) {
		if (client == null || !client.isLoggedIn()) {
			sendMessage(event, "No hay una sesión iniciada.");
			return;
		}

		final List<SidecarInfo> album = new ArrayList<>();

		for (File f : fileQueue) {
			album.add(SidecarPhoto.from(f));
		}

		if (album.size() <= 1) {
			sendMessage(event, Messages.ALBUM_WRONG_SIZE.getMessage());
			return;
		}

		client.actions().timeline().uploadAlbum(album, String.format("%s", processCaptions(captions))).thenAccept(t -> {
			sendMessage(event, Messages.ALBUM_UPLOADED.getMessage());
		}).exceptionally((t) -> {
			sendMessage(event, String.format("Ocurrió un error: %s", t.getMessage()));
			t.printStackTrace();
			return null;
		}).join();
	}

	private void uploadScheduled(SlashCommandInteractionEvent event) {
		scheduledTask = true;
		LocalDateTime now = LocalDateTime.now();

		int day = parameters.containsKey("day") ? (int) parameters.get("day") : now.getDayOfMonth();
		int month = parameters.containsKey("month") ? (int) parameters.get("month") : now.getMonthValue();
		int year = parameters.containsKey("year") ? (int) parameters.get("year") : now.getYear();

		int hour = parameters.containsKey("hour") ? (int) parameters.get("hour") : now.getHour();
		int minute = parameters.containsKey("minute") ? (int) parameters.get("minute") : now.getMinute();

		String captions = processCaptions(parameters.get("captions").toString());
		boolean isAlbum = (boolean) parameters.get("is_album");

		LocalDateTime dateToSchedule = wellFormedDate(year, month, day, hour, minute);

		if (dateToSchedule == null) {
			System.out.println("Fecha mal formada.");
			sendMessage(event, "Fecha mal formada.");
			return;
		}

		dateToSchedule.minusSeconds(dateToSchedule.getSecond());
		System.out.printf("Post programado para: %s %n", formatLocalDateTime(dateToSchedule));

		thread = new Thread(() -> {
			try {
				while (LocalDateTime.now().isBefore(dateToSchedule)) {
					Thread.sleep(30000);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (isAlbum) {
				uploadAlbum(event, captions);
				clearQueue(event);
			} else {
				uploadFile(event);
			}

			scheduledTask = false;
		});

		thread.start();

		sendMessage(event, String.format("%s programado para: %s.", isAlbum ? "Álbum" : "Post",
				formatLocalDateTime(dateToSchedule)));
	}

	private void addImageToQueue(SlashCommandInteractionEvent event, Attachment att) {
		if (!att.isImage()) {
			return;
		}

		File f = saveFile(event, att);
		fileQueue.add(f);
		sendMessage(event, Messages.ATTACHMENT_ADDED_QUEUE.getMessage());
	}

	private File saveFile(SlashCommandInteractionEvent event, Attachment att) {
		File f = new File(att.getId());

		try (DataOutputStream dosAtt = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f)))) {
			URL urlAtt = new URI(att.getUrl()).toURL();
			InputStream is = urlAtt.openStream();

			dosAtt.write(is.readAllBytes());

			return f;
		} catch (MalformedURLException | URISyntaxException e) {
			e.printStackTrace();
			sendMessage(event, Messages.GENERIC.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			sendMessage(event, Messages.GENERIC_FILE_ERROR.getMessage());
		}

		return null;
	}

	private void clearQueue(SlashCommandInteractionEvent event) {
		if (scheduledTask) {
			sendMessage(event, Messages.CANNOT_CLEAR_QUEUE.getMessage());
			return;
		}

		for (File f : fileQueue) {
			if (f.exists()) {
				f.delete();
			}
		}

		fileQueue.clear();
		sendMessage(event, Messages.CLEARED_QUEUE.getMessage());
	}

	private void sendMessage(SlashCommandInteractionEvent event, String message) {
		event.getHook().sendMessage(message).queue();
	}

}
