package com.comduck.chatbot.discord.action.reactions;

import com.comduck.chatbot.discord.BotInstance;
import com.comduck.chatbot.discord.action.Command;
import com.comduck.chatbot.discord.action.ReactionCommand;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@ReactionCommand(name = "\uD83D\uDDD1\uFE0F")
public class RemoveBotMassege implements Command {
    @Override
    public void OnCommand(BotInstance instance, GenericEvent e, String msg, boolean isAdd) throws SQLException, ClassNotFoundException, IOException, ParseException, SpotifyWebApiException {
        GenericMessageReactionEvent event = (GenericMessageReactionEvent) e;

        if(event.getReaction().isSelf()) return;


        if(!event.isFromGuild())
        {
            var privateCh = event.getChannel().asPrivateChannel();
            privateCh.deleteMessageById(event.getMessageId()).queue();
            return;
        }

        event.getChannel().retrieveMessageById(event.getMessageId()).queue(message -> {
            if(message.getAuthor().isBot())
            {
                message.delete().queue();
            }
        });
    }

    @Override
    public void OnPostCommand(BotInstance instance, GenericEvent e) {

    }
}
