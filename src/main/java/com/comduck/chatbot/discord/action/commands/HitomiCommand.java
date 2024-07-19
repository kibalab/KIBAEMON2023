package com.comduck.chatbot.discord.action.commands;

import com.comduck.chatbot.discord.BotInstance;
import com.comduck.chatbot.discord.action.Category;
import com.comduck.chatbot.discord.action.Command;
import com.comduck.chatbot.discord.action.MessageCommand;
import com.comduck.chatbot.discord.api.hitomiapi.HitomiLoader;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.hc.core5.http.ParseException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.DefaultHttpClient;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@MessageCommand(name = {"hitomi"}, parm = {"galleryID"}, desc = "히토미 다운로더 입니다.", cat = Category.API)
public class HitomiCommand implements Command {

    @Override
    public void OnCommand(BotInstance instance, GenericEvent e, String msg, boolean isAdd) throws SQLException, ClassNotFoundException, IOException, ParseException, SpotifyWebApiException, URISyntaxException {
        var event = (MessageReceivedEvent) e;

        AtomicReference<Message> statusMsg = new AtomicReference<>();
        event.getChannel().sendMessage("> Loading...").queue((a) -> {
            statusMsg.set(a);
        });

        var id = msg.replace("hitomi", "").replace(" ", "");

        event.getMessage().delete().queue();

        var loader = new HitomiLoader(id,
                (index) -> {
                    statusMsg.get().editMessage("> Collecting : " + index + "pages" ).queue();
                    //channel.get().sendMessage("Page : " + page.get()).addFiles(images).queue();
                },
                (images) -> {
                    try {
                        statusMsg.get().editMessage("> CDN URL Generating...").queue();

                        var fileName = id+".zip";
                        String zipPath = "./"+fileName;
                        var zip = new ZipOutputStream(new FileOutputStream(zipPath));

                        var i = 0;
                        for (var image : images)
                        {
                            if(image == null) continue;
                            ZipEntry zipItem = new ZipEntry(i + ".jpg");
                            zip.putNextEntry(zipItem);
                            zip.write(image);
                            i++;
                        }
                        zip.close();

                        var zipFile = new File(zipPath);
                        var stream = new FileInputStream(zipFile);

                        byte[] data = stream.readAllBytes();
                        HttpClient client = new DefaultHttpClient();
                        HttpPost httpPost = new HttpPost("http://www.kiba.red:4242/file/upload");

                        ByteArrayBody byteArrayBody = new ByteArrayBody(data, "application/multipart", fileName);
                        MultipartEntity multipartEntity = new MultipartEntity();
                        multipartEntity.addPart("file", byteArrayBody);
                        httpPost.setEntity( multipartEntity );

                        HttpResponse response = client.execute(httpPost);
                        System.out.println(response);

                        stream.close();
                        zipFile.delete();

                        statusMsg.get().editMessage("> " + id + "\nZip Download : " + "http://www.kiba.red:4242/file/download?filename=" + fileName).queue();
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                });

        loader.start();
    }

    @Override
    public void OnPostCommand(BotInstance instance, GenericEvent e) {

    }
}
