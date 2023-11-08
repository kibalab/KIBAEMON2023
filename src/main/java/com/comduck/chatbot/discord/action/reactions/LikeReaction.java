package com.comduck.chatbot.discord.action.reactions;

import com.comduck.chatbot.database.MariaDB;
import com.comduck.chatbot.database.Table;
import com.comduck.chatbot.discord.BotInstance;
import com.comduck.chatbot.discord.action.Command;
import com.comduck.chatbot.discord.action.ReactionCommand;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@ReactionCommand(name = "⭐")
public class LikeReaction implements Command {
    @Override
    public void OnCommand(BotInstance instance, GenericEvent e, String msg, boolean isAdd) throws SQLException, ClassNotFoundException {
        GenericMessageReactionEvent event = (GenericMessageReactionEvent) e;

        if(!isAdd)
        {
            return;
        }
        event.getChannel().retrieveMessageById(event.getMessageId()).queue( m -> {
            for(MessageReaction reaction : m.getReactions()) {
                if(reaction.getReactionEmote().getEmoji().equals("⭐"))
                {
                    if(reaction.getCount() >= 5) {
                        try {
                            ResultSet r = MariaDB.Get(Table.LIKEMSG, "message_id", event.getMessageId());
                            if(!r.next()){
                                HashMap<String, Object> data = new HashMap<>();
                                data.put("message_id", event.getMessageId());
                                data.put("channel_id", event.getChannel().getId());
                                data.put("server_id", event.getGuild().getId());
                                MariaDB.Add(Table.LIKEMSG, data);

                                ResultSet r1 = MariaDB.Get(Table.SERVER_SETTINGS, "server_id", event.getGuild().getId());
                                if(r1.next()) {
                                    TextChannel ch = event.getGuild().getTextChannelById(r1.getString("like_ch_id"));
                                    CopyMessage(event, ch);
                                }
                            }
                            break;
                        } catch (SQLException ex) {
                            throw new RuntimeException(ex);
                        } catch (ClassNotFoundException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
            }

        });
    }

    @Override
    public void OnPostCommand(BotInstance instance, GenericEvent e) {

    }

    public void CopyMessage(GenericMessageReactionEvent event, MessageChannel dest)
    {
        event.getChannel().retrieveMessageById(event.getMessageId()).queue(message -> {
            String msgLink = "https://discord.com/channels/"+event.getGuild().getId()+"/"+event.getChannel().getId()+"/"+event.getMessageId();
            for (Message.Attachment a :message.getAttachments()) {
                try {
                    dest.sendFile(a.retrieveInputStream().get(30, TimeUnit.SECONDS), a.getFileName()).queue();
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                } catch (ExecutionException ex) {
                    throw new RuntimeException(ex);
                } catch (TimeoutException ex) {
                    throw new RuntimeException(ex);
                }
            }
            dest.sendMessage(msgLink + "\n" + message.getContentRaw()).queue();
        });
    }
}
