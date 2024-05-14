package com.comduck.chatbot.discord.action.commands;

import com.comduck.chatbot.database.MariaDB;
import com.comduck.chatbot.database.Table;
import com.comduck.chatbot.discord.BotInstance;
import com.comduck.chatbot.discord.action.Category;
import com.comduck.chatbot.discord.action.Command;
import com.comduck.chatbot.discord.action.MessageCommand;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@MessageCommand(name="loadbm", parm = {"ServerID"}, desc = "다른 서버에 저장된 북마크를 불러옵니다.", cat= Category.Social)
public class LoadBookmarkCommand implements Command {
    @Override
    public void OnCommand(BotInstance instance, GenericEvent e, String msg, boolean isAdd) throws SQLException, ClassNotFoundException {
        MessageReceivedEvent event = (MessageReceivedEvent) e;
        String catid = msg.replaceFirst("loadbm", "").replace(" ", "");

        ResultSet r = MariaDB.Get(Table.BOOKMARK, "server_id", event.getGuild().getId());

        ResultSet r1 = MariaDB.Get(Table.SERVER_SETTINGS, "server_id", event.getGuild().getId());
        if(!r1.next()) return;
        String cat = r1.getString("bm_cat_id");

        TextChannel channel = null;
        for (TextChannel ch : event.getGuild().getCategoryById(cat).getTextChannels())
        {
            if(ch.getTopic() == null) continue;
            if(ch.getTopic().startsWith(event.getAuthor().getId())) channel = ch;
        }

        while(r.next())
        {
            String msg_id = r.getString("message_id");
            TextChannel finalChannel = channel;
            event.getGuild().retrieveMemberById(msg_id).queue(m -> {
                CopyMessage((GenericMessageReactionEvent) e, finalChannel);
            });
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
