package com.comduck.chatbot.discord.action.commands;

import com.comduck.chatbot.discord.BotInstance;
import com.comduck.chatbot.discord.action.Category;
import com.comduck.chatbot.discord.action.Command;
import com.comduck.chatbot.discord.action.MessageCommand;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;

@MessageCommand(name = {"ping", "핑", "speed", "속도"}, order = 0, desc = "봇과 디스코드 서버간의 통신 지연을 측정합니다.", cat= Category.API)
public class PingCommand implements Command {

    @Override
    public void OnCommand(BotInstance instance, GenericEvent e, String msg, boolean isAdd) throws SQLException, ClassNotFoundException, IOException, ParseException, SpotifyWebApiException {
        var event = (MessageReceivedEvent) e;
        var created = event.getMessage().getTimeCreated();
        var now = OffsetDateTime.now().toInstant();
        var t = Duration.between(instance.lastDateTime.toInstant(), created.toInstant()).toMillisPart();
        var t2 = Duration.between(instance.lastDateTime.toInstant(), now).toMillisPart();
        event.getChannel().sendMessage( String.format("> Pong!\n> From Discord : `%d ms`\n> Processing : `%d ms`", t, t2)).queue();
    }

    @Override
    public void OnPostCommand(BotInstance instance, GenericEvent e) {

    }
}
