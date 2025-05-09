package discord_bot;

import java.io.File;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.github.instagram4j.instagram4j.actions.timeline.TimelineAction.SidecarInfo;
import com.github.instagram4j.instagram4j.actions.timeline.TimelineAction.SidecarPhoto;
import com.github.instagram4j.instagram4j.exceptions.IGResponseException;

import discord_bot.controller.DateManager;
import discord_bot.controller.FileManager;
import discord_bot.controller.MessageManager;
import discord_bot.controller.SessionManager;
import discord_bot.model.Messages;
import discord_bot.model.Parameters;
import discord_bot.model.publications.Album;
import discord_bot.model.publications.Post;
import discord_bot.model.publications.Scheduled;
import discord_bot.model.publications.ScheduledAlbum;
import discord_bot.model.publications.ScheduledPost;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class Bot extends ListenerAdapter {
	public static final String VERSION = "1.35 BETA";
	private static final int MAX_SCHEDULED_PUBLICATION = 7;
	private static final int MIN_ALBUM_SIZE = 2;
	private static final Duration TIME_TO_WAIT = Duration.ofSeconds(1);

	private final List<Scheduled> scheduledPublications = new ArrayList<>();
//	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(MAX_SCHEDULED_PUBLICATION);

	private Album currentAlbum;
	private Member memberUsingBot;

	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		if (memberUsingBot != null && !memberUsingBot.equals(event.getMember())) {
			MessageManager.sendMessage(event, Messages.SOMEONE_USING_BOT);
			return;
		}

		event.deferReply(true).queue();

		switch (event.getName().toLowerCase()) {
		case "login" -> {
			if (SessionManager.getClient() != null && SessionManager.getClient().isLoggedIn()) {
				System.out.println("[!] Ya hay alguien con una sesión.");
				MessageManager.sendMessage(event, Messages.ALREADY_LOGGED);
				return;
			}

			String username = event.getOption(Parameters.username.name(), OptionMapping::getAsString);
			String password = event.getOption(Parameters.password.name(), OptionMapping::getAsString);
			String verificationCode = event.getOption(Parameters.verification_code.name(), OptionMapping::getAsString);

			System.out.println("[+] Iniciando sesión...");
			if (SessionManager.login(event, username, password, verificationCode)) {
				this.memberUsingBot = event.getMember();
				System.out.println("[+] Sesión iniciada.");
			} else {
				System.out.println("[!] No se pudo iniciar sesión.");
			}
		}
		case "logout" -> {
			System.out.println("[-] Cerrando sesión...");
			SessionManager.logout(event, scheduledPublications.isEmpty());
		}
		case "add_image" -> {
			if (currentAlbum == null) {
				System.out.println("[+] Creando un nuevo álbum.");
				currentAlbum = new Album(event, null);
			}

			Attachment att = event.getOption(Parameters.attachment.name(), OptionMapping::getAsAttachment);

			if (!att.isImage()) {
				MessageManager.sendMessage(event, "El archivo **DEBE** ser una imagen.");
				return;
			}

			System.out.println("[+] Guardando imagen... (add_image)");

			File f = FileManager.saveFile(event, att);

			if (f == null) {
				MessageManager.sendMessage(event, Messages.GENERIC_FILE_ERROR);
				return;
			}

			System.out.println("[+] Añadiendo imagen a álbum...");
			currentAlbum.addFile(f);

			MessageManager.sendMessage(event, Messages.ATTACHMENT_ADDED_QUEUE);

		}
		case "clear_queue" -> {
			if (currentAlbum == null) {
				MessageManager.sendMessage(event, "No hay ningún álbum creado.");
				return;
			}

			System.out.println("[+] Vaciando cola de archivos...");
			currentAlbum.clearFiles();
		}
		case "upload_post" -> {
			Attachment attachment = event.getOption(Parameters.attachment.name(), OptionMapping::getAsAttachment);
			Attachment cover = event.getOption(Parameters.cover.name(), OptionMapping::getAsAttachment);

			System.out.println("[+] Guardando imágenes... (post).");
			File attFile = FileManager.saveFile(event, attachment);
			File coverFile = FileManager.saveFile(event, cover);

			coverFile = Objects.requireNonNullElse(coverFile, attFile);

			System.out.println("[+] Imágenes guardadas (post).");

			String captions = processCaptions(event.getOption(Parameters.captions.name(), OptionMapping::getAsString));

			Post p = new Post(event, attFile, coverFile, attachment, cover, captions, false);

			System.out.println("[+] Subiendo post...");
			uploadFile(event, p);
		}
		case "upload_scheduled_post" -> {
			ZonedDateTime dateToSchedule = getDateFromParameters(event);

			if (publicationHasCorrectDate(dateToSchedule)) {
				MessageManager.sendMessage(event, String.format("Ya hay una publicación programada para la fecha %s.",
						DateManager.formatDate(dateToSchedule)));
				return;
			}

			Attachment att = event.getOption(Parameters.attachment.name(), OptionMapping::getAsAttachment);
			Attachment cover = event.getOption(Parameters.cover.name(), OptionMapping::getAsAttachment);
			String captions = event.getOption(Parameters.captions.name(), OptionMapping::getAsString);

			System.out.println("[+] Guardando imagen... (scheduled post).");

			File attFile = FileManager.saveFile(event, att);
			File coverFile = FileManager.saveFile(event, cover);

			coverFile = Objects.requireNonNullElse(coverFile, attFile);

			ScheduledPost sp = new ScheduledPost(event, attFile, coverFile, att, cover, captions, dateToSchedule);

			System.out.println("[+] Añadiendo a la lista (scheduled post).");

			scheduledPublications.add(sp);

			uploadScheduled(event, sp);
			MessageManager.sendMessage(event,
					String.format("Post programado para: %s %n", DateManager.formatDate(dateToSchedule)));
		}

		case "upload_album" -> {
			if (currentAlbum == null || currentAlbum.getFiles().size() < MIN_ALBUM_SIZE) {
				System.out.println("[!] Tienes que añadir dos o más imágenes.");
				MessageManager.sendMessage(event, "Tienes que añadir dos o más imágenes.");
				return;
			}

			String captions = processCaptions(event.getOption(Parameters.captions.name(), OptionMapping::getAsString));

			currentAlbum.setCaptions(captions);

			System.out.println("[+] Subiendo álbum...");
			uploadAlbum(event, currentAlbum);
		}

		case "upload_scheduled_album" -> {
			// No se llama al método "saveFile()", porque al añadir a la cola las imágenes,
			// ya se llama a ese método.System.out.println(currentAlbum == null);
			if (currentAlbum == null || currentAlbum.getFiles().size() < MIN_ALBUM_SIZE) {
				System.out.println("[!] Tienes que añadir dos o más imágenes.");
				MessageManager.sendMessage(event, "Tienes que añadir dos o más imágenes.");
				return;
			}

			ZonedDateTime dateToSchedule = getDateFromParameters(event);

			if (publicationHasCorrectDate(dateToSchedule)) {
				MessageManager.sendMessage(event, String.format("Ya hay una publicación programada para la fecha %s.",
						DateManager.formatDate(dateToSchedule)));
				return;
			}

			String captions = processCaptions(event.getOption(Parameters.captions.name(), OptionMapping::getAsString));

			ScheduledAlbum sa = new ScheduledAlbum(event, captions, dateToSchedule);
			sa.addAllFiles(currentAlbum.getFiles());
			currentAlbum = null;

			scheduledPublications.add(sa);

			MessageManager.sendMessage(event,
					String.format("Álbum programado para: %s %n", DateManager.formatDate(dateToSchedule)));

			uploadScheduled(event, sa);
		}
		case "upload_storie" -> {
			Attachment att = event.getOption(Parameters.attachment.name(), OptionMapping::getAsAttachment);

			System.out.println("[+] Guardando imagen... (storie).");
			File attFile = FileManager.saveFile(event, att);

			Post p = new Post(event, attFile, null, att, null, "", true);

			System.out.println("[+] Subiendo storie...");
			uploadFile(event, p);
		}

		case "show_queue" -> {
			if (scheduledPublications.isEmpty()) {
				MessageManager.sendMessage(event, "No hay álbumes programados.");
				return;
			}

			Integer index = event.getOption(Parameters.index.name(), OptionMapping::getAsInt);
			index = index == null ? 0 : Math.clamp(index, 0, scheduledPublications.size() - 1);

			System.out.println(index);
			Scheduled s = scheduledPublications.get(index);

			if (s instanceof Album) {
				System.out.printf("[!] Mostrando álbum: %s %n", s);
				MessageManager.sendFiles(event, (Album) s);
			} else {
				MessageManager.sendMessage(event, "En el índice seleccionado no hay un álbum.");
			}
		}

		case "queue_size" -> {
			MessageManager.sendMessage(event, String.format("Hay %d/%d publicaciones programadas.",
					scheduledPublications.size(), MAX_SCHEDULED_PUBLICATION));
		}
		}
	}

	private void uploadScheduled(SlashCommandInteractionEvent event, Scheduled scheduled) {
		if (scheduledPublications.size() >= MAX_SCHEDULED_PUBLICATION) {
			MessageManager.sendMessage(event,
					String.format("Ya se ha alcanzado el valor máximo de publicaciones programadas (%d).",
							scheduledPublications.size()));
			return;
		}

		new Thread(() -> {
			final ZonedDateTime dateToSchedule = scheduled.getDate();

			while (DateManager.now().isBefore(dateToSchedule)) {
				try {
					Thread.sleep(TIME_TO_WAIT);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			System.out.printf("[+] Post/álbumes programados: %s %n", scheduledPublications);

			if (scheduled instanceof ScheduledPost) {
				uploadFile(event, (ScheduledPost) scheduled);
			} else {
				uploadAlbum(event, (ScheduledAlbum) scheduled);
			}
		}).start();

//		final ZonedDateTime now = DateManager.now();
//		final Duration duration = Duration.between(now, scheduled.getDate());
//
//		scheduler.schedule(() -> {
//			System.out.printf("[+] Post/álbumes programados: %s %n", scheduledPublications);
//
//			if (scheduled instanceof ScheduledPost) {
//				uploadFile(event, (ScheduledPost) scheduled);
//			} else {
//				uploadAlbum(event, (ScheduledAlbum) scheduled);
//			}
//		}, duration.toSeconds(), TimeUnit.SECONDS);

	}

	private void uploadFile(SlashCommandInteractionEvent event, Post p) {
		boolean isVideo = p.getAttachment().isVideo();
		boolean isStorie = p.isStorie();
		File attFile = p.getAttachmentFile();
		File coverFile = p.getCoverFile();
		String captions = p.getCaptions();

		if (SessionManager.getClient() == null || !SessionManager.getClient().isLoggedIn()) {
			MessageManager.sendMessage(event, "No hay una sesión iniciada.");
			return;
		}

		if (isVideo) {
			if (isStorie) {
				SessionManager.getClient().actions().story().uploadVideo(attFile, coverFile).thenAccept(t -> {
					MessageManager.sendMessage(event, Messages.VIDEO_STORIE_UPLOADED);
				}).exceptionally(t -> exceptionHandler(event, t)).join();
			} else {
				SessionManager.getClient().actions().timeline().uploadVideo(attFile, coverFile, captions)
						.thenAccept(t -> {
							MessageManager.sendMessage(event, Messages.VIDEO_POST_UPLOADED);

							if (p instanceof ScheduledPost) {
								scheduledPublications.remove((ScheduledPost) p);
								System.gc();
							}
						}).exceptionally(t -> exceptionHandler(event, t)).join();
			}
		} else {
			if (isStorie) {
				SessionManager.getClient().actions().story().uploadPhoto(attFile).thenAccept(t -> {
					MessageManager.sendMessage(event, Messages.IMAGE_STORIE_UPLOADED);
				}).exceptionally(t -> exceptionHandler(event, t)).join();
			} else {
				SessionManager.getClient().actions().timeline().uploadPhoto(attFile, captions).thenAccept(t -> {
					MessageManager.sendMessage(event, Messages.IMAGE_POST_UPLOADED);

					if (p instanceof ScheduledPost) {
						scheduledPublications.remove((ScheduledPost) p);
						System.gc();
					}
				}).exceptionally(t -> exceptionHandler(event, t)).join();
			}
		}

		System.out.printf("[+] %s subido correctamente. %n", isStorie ? "Storie" : "Post");

		FileManager.deleteFile(attFile);
		FileManager.deleteFile(coverFile);
	}

	private void uploadAlbum(SlashCommandInteractionEvent event, Album album) {
		if (SessionManager.getClient() == null || !SessionManager.getClient().isLoggedIn()) {
			MessageManager.sendMessage(event, "No hay una sesión iniciada.");
			return;
		} else if (album.getFiles().size() < MIN_ALBUM_SIZE) {
			MessageManager.sendMessage(event, "Sólo se pueden subir álbumes con dos o más imágenes.");
			return;
		}

		final List<SidecarInfo> albumSidecarInfo = new ArrayList<>();

		for (File f : album.getFiles()) {
			albumSidecarInfo.add(SidecarPhoto.from(f));
		}

		SessionManager.getClient().actions().timeline()
				.uploadAlbum(albumSidecarInfo, processCaptions(album.getCaptions())).thenAccept(t -> {
					MessageManager.sendMessage(event, Messages.ALBUM_UPLOADED);
					System.out.println("[+] Álbum subido correctamente.");

					if (album instanceof ScheduledAlbum) {
						scheduledPublications.remove((ScheduledAlbum) album);
						System.gc();
					}
				}).exceptionally(t -> exceptionHandler(event, t)).join();

	}

	private ZonedDateTime getDateFromParameters(SlashCommandInteractionEvent event) {
		final ZonedDateTime now = DateManager.now();

		int day = Objects.requireNonNullElse(event.getOption(Parameters.day.name(), OptionMapping::getAsInt),
				now.getDayOfMonth());
		int month = Objects.requireNonNullElse(event.getOption(Parameters.month.name(), OptionMapping::getAsInt),
				now.getMonthValue());
		int year = Objects.requireNonNullElse(event.getOption(Parameters.year.name(), OptionMapping::getAsInt),
				now.getYear());

		int hour = Objects.requireNonNullElse(event.getOption(Parameters.hour.name(), OptionMapping::getAsInt),
				now.getHour());
		int minute = Objects.requireNonNullElse(event.getOption(Parameters.minute.name(), OptionMapping::getAsInt),
				now.getMinute());

		return Objects.requireNonNullElse(
				ZonedDateTime.of(year, month, day, hour, minute, 0, 0, DateManager.SPAIN_ZONE), now);
	}

	private String processCaptions(String captions) {
		if (!captions.contains("%s")) {
			return captions;
		}

		return captions.replaceAll("%s", "\r\n");
	}

	private boolean publicationHasCorrectDate(ZonedDateTime zdt) {
		for (Scheduled s : scheduledPublications) {
			if (s.getDate().equals(zdt)) {
				return true;
			}
		}

		return false;
	}

	private Void exceptionHandler(SlashCommandInteractionEvent event, Throwable t) {
		if (t.getCause() instanceof IGResponseException) {
			IGResponseException igResponseException = (IGResponseException) t.getCause();

			MessageManager.sendMessage(event,
					String.format("Respuesta de Instagram: %s.%n(Probablemente sea una mala resolución de la imagen).",
							igResponseException.getMessage()));
			igResponseException.printStackTrace();
			System.out.println("Mensaje: " + igResponseException.getMessage());
			System.out.println(igResponseException);
		} else {
			MessageManager.sendMessage(event,
					String.format("Ocurrió un error: %s.%n(Probablemente sea una mala resolución de la imagen).", t));
			t.printStackTrace();
		}

		return null;
	}

}
