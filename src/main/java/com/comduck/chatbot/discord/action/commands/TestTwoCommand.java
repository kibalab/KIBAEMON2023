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

@MessageCommand(name = "testTwo", cat = Category.Hide)
public class TestTwoCommand implements Command {
    @Override
    public void OnCommand(BotInstance instance, GenericEvent e, String msg, boolean isAdd) throws SQLException, ClassNotFoundException, IOException, ParseException, SpotifyWebApiException {
        ((MessageReceivedEvent)e).getChannel().sendMessage("Hello, World!").queue();
    }

    @Override
    public void OnPostCommand(BotInstance instance, GenericEvent e) {

    }
}
