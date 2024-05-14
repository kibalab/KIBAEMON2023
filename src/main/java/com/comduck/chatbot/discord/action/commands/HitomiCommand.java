package com.comduck.chatbot.discord.action.commands;

import com.comduck.chatbot.discord.BotInstance;
import com.comduck.chatbot.discord.action.Category;
import com.comduck.chatbot.discord.action.Command;
import com.comduck.chatbot.discord.action.MessageCommand;
import com.comduck.chatbot.discord.api.hitomiapi.HitomiLoader;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@MessageCommand(name = {"hitomi"}, desc = "히토미 다운로더 입니다.", cat = Category.API)
public class HitomiCommand implements Command {

    @Override
    public void OnCommand(BotInstance instance, GenericEvent e, String msg, boolean isAdd) throws SQLException, ClassNotFoundException, IOException, ParseException, SpotifyWebApiException, URISyntaxException {
        var event = (MessageReceivedEvent) e;

        event.getChannel().sendMessage("> DM으로 전달해드리겠습니다!").queue();

        var id = msg.replace("hitomi", "").replace(" ", "");

        AtomicReference<PrivateChannel> channel = new AtomicReference<>();
        AtomicBoolean sendable = new AtomicBoolean();
        event.getAuthor().openPrivateChannel().queue((ch) -> {
            channel.set(ch);
            try {
                channel.get().sendMessage(String.format("Loading : %s", id)).queue();
                sendable.set(true);
            } catch (Exception ex) {
                event.getChannel().sendMessage("> DM을 보낼수 없습니다. 계정 설정을 변경해주세요.");
                sendable.set(false);
                ex.printStackTrace();
            }
        });

        if(!sendable.get()) return;

        AtomicInteger page = new AtomicInteger(0);
        var loader = new HitomiLoader(id, (images) -> {
            page.getAndIncrement();
            channel.get().sendMessage("Page : " + page.get()).addFiles(images).queue();
        });

        loader.start();
    }

    @Override
    public void OnPostCommand(BotInstance instance, GenericEvent e) {

    }
}
