package discord_bot.controller;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class BDController {
	private static final File DB = new File("scheduled_posts.db");
	private static boolean initialized = false;
	private static Connection conn;

	public static String init() {
		initialized = true;
		String msg;

		try {
			msg = String.format("jdbc:sqlite:%s", DB.getPath());

			if (!DB.exists()) {
				DB.createNewFile();
			}

			conn = DriverManager.getConnection(String.format("jdbc:sqlite:%s", DB.getPath()));

		} catch (IOException | SQLException e) {
			e.printStackTrace();
			msg = e.getMessage();
		}

		return msg;
	}

	public static void main(String[] args) {
		System.out.println(BDController.init());

	}
}
