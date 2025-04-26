package discord_bot.controller;

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

import discord_bot.model.Messages;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class FileManager {
    public static File saveFile(SlashCommandInteractionEvent event, Attachment att) {
        if (att == null) {
            return null;
        }

        try {
            File f = File.createTempFile(att.getId(), null);
            DataOutputStream dosAtt = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
            URL urlAtt = new URI(att.getUrl()).toURL();
            InputStream is = urlAtt.openStream();

            dosAtt.write(is.readAllBytes());

            dosAtt.close();

            return f;
        } catch (MalformedURLException | URISyntaxException e) {
            e.printStackTrace();
            MessageManager.sendMessage(event, Messages.GENERIC.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            MessageManager.sendMessage(event, Messages.GENERIC_FILE_ERROR.getMessage());
        }

        return null;
    }

    public static void deleteFile(File file) {
        if (file == null) {
            System.out.println("[!] Archivo nulo.");
            return;
        }

        System.out.printf("[-] Borrando archivo: %s. %n", file.getName());
        if (file.exists()) {
            System.out.printf(file.delete() ? "[-] Archivo borrado correctamente: %s. %n" : "[!] No se pudo borrar el archivo %s. %n", file.getName());
        }
    }

}
