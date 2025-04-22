package discord_bot.controller;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateManager {
	public static final ZoneId SPAIN_ZONE = ZoneId.of("Asia/Tokyo");
	public static final ZonedDateTime DEPLOY_DATE = ZonedDateTime.now(SPAIN_ZONE);

	private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

	public static String formatDate(ZonedDateTime date) {
		return date.format(dtf);
	}

	public static String getDeployDate() {
		return formatDate(DEPLOY_DATE);
	}

	public static ZonedDateTime now() {
		return ZonedDateTime.now(SPAIN_ZONE);
	}
}
