package discord_bot.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class MyDateFormatter {
	private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

	public static String formatDate(LocalDateTime date) {
		return date.format(dtf);
	}
}
