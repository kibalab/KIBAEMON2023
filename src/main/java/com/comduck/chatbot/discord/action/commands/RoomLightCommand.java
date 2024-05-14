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
import java.util.Random;

@MessageCommand(name = {"light", "불", "光"}, desc = "불을 켜거나 끕니다.", cat = Category.ETC)
public class RoomLightCommand implements Command {

    static boolean state = false;

    @Override
    public void OnCommand(BotInstance instance, GenericEvent e, String msg, boolean isAdd) throws SQLException, ClassNotFoundException, IOException, ParseException, SpotifyWebApiException {
        var event = (MessageReceivedEvent) e;
        state = !state;

        event.getChannel().sendMessage(state ? "불을 켰어요!" : "불을 껐어요").queue();
    }

    @Override
    public void OnPostCommand(BotInstance instance, GenericEvent e) {

    }
}
