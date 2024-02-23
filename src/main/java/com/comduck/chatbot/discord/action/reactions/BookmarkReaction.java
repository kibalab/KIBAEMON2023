package com.comduck.chatbot.discord.action.reactions;

import com.comduck.chatbot.database.MariaDB;
import com.comduck.chatbot.database.Table;
import com.comduck.chatbot.discord.BotInstance;
import com.comduck.chatbot.discord.action.Command;
import com.comduck.chatbot.discord.action.ReactionCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.requests.restaction.ChannelAction;
import net.dv8tion.jda.api.utils.FileUpload;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@ReactionCommand(name = "\uD83D\uDD16") // :bookmark:
public class BookmarkReaction implements Command {
    @Override
    public void OnCommand(BotInstance instance, GenericEvent e, String msg, boolean isAdd) throws ClassNotFoundException {
        GenericMessageReactionEvent event = (GenericMessageReactionEvent) e;

        // 현재 날짜와 시간 가져오기
        Date currentDate = new Date();
        Timestamp currentTimestamp = new Timestamp(currentDate.getTime());

        try {
            HashMap<String, Object> data = new HashMap<>();
            data.put("message_id", event.getMessageId());
            data.put("author_id", event.getUserId());
            data.put("channel_id", event.getChannel().getId());
            data.put("server_id", event.getGuild().getId());
            data.put("datetime", currentTimestamp.toString());
            MariaDB.Add(Table.BOOKMARK, data);
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }

        try {
            ResultSet r = MariaDB.Get(Table.SERVER_SETTINGS, "server_id", event.getGuild().getId());
            event.getUser().openPrivateChannel().queue(privateChannel -> CopyMessage(event, privateChannel));
            if(!r.next()) return;
            String cat = r.getString("bm_cat_id");

            TextChannel channel = null;
            for (TextChannel ch : event.getGuild().getCategoryById(cat).getTextChannels())
            {
                if(ch.getTopic() == null) continue;
                if(ch.getTopic().startsWith(event.getUserId())) channel = ch;
            }
            if(channel == null) {
                ChannelAction action = event.getGuild().createTextChannel(event.getUser().getName());
                action.setTopic(event.getUserId());
                action.setNSFW(true);
                action.setParent(event.getGuild().getCategoryById(cat));

                Collection<Permission> empty = new ArrayList<>();
                Collection<Permission> perm = new ArrayList<>();
                perm.add(Permission.MESSAGE_SEND);
                perm.add(Permission.VIEW_CHANNEL);

                action.addPermissionOverride(event.getGuild().getPublicRole(), empty, perm);
                for (Member member : event.getGuild().getMembers()) {
                    if(member.getId().equals(event.getUserId()) || member.getUser().isBot()) {
                        action.addMemberPermissionOverride(member.getIdLong(), perm, empty);
                    }
                }
                action.queue( o -> {
                    CopyMessage(event, (MessageChannel) o);
                });
            }
            else {
                CopyMessage(event, channel);
            }

        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
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
                    dest.sendFiles(FileUpload.fromData(a.retrieveInputStream().get(30, TimeUnit.SECONDS), a.getFileName())).queue();
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
