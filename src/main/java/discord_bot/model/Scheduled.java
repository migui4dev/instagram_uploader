package discord_bot.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Scheduled {
	private LocalDateTime date;
	private String captions;
	private ArrayList<Long> filesId;

	public Scheduled() {
		this.filesId = new ArrayList<>();
	}

	public Scheduled(LocalDateTime dateToSchedule, String captions, Long... filesId) {
		super();
		this.date = dateToSchedule;
		this.captions = captions;
		this.filesId = new ArrayList<>(Arrays.asList(filesId));
	}

	public LocalDateTime getDate() {
		return date;
	}

	public String getCaptions() {
		return captions;
	}

	public ArrayList<Long> getFilesName() {
		return filesId;
	}

	public void addFileId(Long fileName) {
		this.filesId.add(fileName);
	}

	public void addFilesId(Long... filesName) {
		this.filesId.addAll(Arrays.asList(filesName));
	}

	public void addFilesId(List<Long> filesName) {
		this.filesId.addAll(filesName);
	}

	@Override
	public String toString() {
		DateTimeFormatter d = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

		return String.format("%s, %s, %s", date.format(d), captions, filesId.toString());
	}

}
