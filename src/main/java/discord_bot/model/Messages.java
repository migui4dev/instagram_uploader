package discord_bot.model;

public enum Messages {

	NULL_FILE("El archivo no existe."), GENERIC_FILE_ERROR("Error de archivo."), GENERIC("Algo ha fallado..."),
	SUCCESS_LOGIN("Inicio de sesión con éxito."), FAILED_LOGIN("Inicio de sesión fallado."),
	ALREADY_LOGGED("Ya hay una sesión iniciada."), SUCCESS_LOGOUT("Cierre de sesión con éxito."),
	VIDEO_STORIE_UPLOADED("Storie (vídeo) subido con éxito."), VIDEO_POST_UPLOADED("Post (vídeo) subido con éxito."),
	IMAGE_STORIE_UPLOADED("Storie (imagen) subida con éxito."), IMAGE_POST_UPLOADED("Post (imagen) subida con éxito."),
	ATTACHMENT_ADDED_QUEUE("Archivo añadido a la cola."), CLEARED_QUEUE("Cola vaciada."),
	CANNOT_CLEAR_QUEUE("Ya hay una tarea programada, no puedes vaciar la cola de archivos."),
	SOMEONE_USING_BOT("No puedes usar el bot, ya lo está usando alguien."),
	NOT_SUPPORTED_FILE("La extensión del archivo no tiene soporte."), ALBUM_UPLOADED("Álbum subido con éxito."),
	ALBUM_WRONG_SIZE("Sólo puedes subir un álbum si tiene dos o más imágenes.");

	private final String msg;

	private Messages(String msg) {
		this.msg = msg;
	}

	public String getMessage() {
		return msg;
	}

}
