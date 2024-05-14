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

@MessageCommand(name = {"water", "물", "水"}, desc = "물을 가져다 드립니다.", cat = Category.ETC)
public class WaterCommand implements Command {

    static int count = 0;

    @Override
    public void OnCommand(BotInstance instance, GenericEvent e, String msg, boolean isAdd) throws SQLException, ClassNotFoundException, IOException, ParseException, SpotifyWebApiException {
        var event = (MessageReceivedEvent) e;
        count++;

        if(count > 10)
        {
            event.getChannel().sendMessage("이런 물이 다 떨어졌어요~").queue();return;
        }
        if(count > 5)
        {
            event.getChannel().sendMessage("아 귀찮아 직접 가져다 마시셈 ㅡㅡ").queue();return;
        }

        event.getChannel().sendMessage(String.format("%s 님께 물을 가져다 드렸습니다!", ((MessageReceivedEvent) e).getAuthor().getName())).queue();
    }

    @Override
    public void OnPostCommand(BotInstance instance, GenericEvent e) {

    }
}
