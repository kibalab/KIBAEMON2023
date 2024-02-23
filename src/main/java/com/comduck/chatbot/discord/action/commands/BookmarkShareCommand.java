package com.comduck.chatbot.discord.action.commands;

import com.comduck.chatbot.database.MariaDB;
import com.comduck.chatbot.database.Table;
import com.comduck.chatbot.discord.BotInstance;
import com.comduck.chatbot.discord.action.Command;
import com.comduck.chatbot.discord.action.MessageCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

@MessageCommand(name = "bmshare")
public class BookmarkShareCommand implements Command {

    @Override
    public void OnCommand(BotInstance instance, GenericEvent e, String msg, boolean isAdd) throws SQLException, ClassNotFoundException {
        MessageReceivedEvent event = (MessageReceivedEvent) e;
        String userid = msg.replaceFirst("bmshare", "").replace(" ", "");

        ResultSet r = MariaDB.Get(Table.SERVER_SETTINGS, "server_id", event.getGuild().getId());
        if(!r.next()) return;
        String cat = r.getString("bm_cat_id");
        TextChannel channel = null;
        for (TextChannel ch : event.getGuild().getCategoryById(cat).getTextChannels())
        {
            if(ch.getTopic() == null) continue;
            if(ch.getTopic().startsWith(event.getAuthor().getId())) channel = ch;
        }
        Collection<Permission> perm = new ArrayList<>();
        perm.add(Permission.VIEW_CHANNEL);

        TextChannel finalChannel = channel;
        event.getGuild().retrieveMemberById(event.getMessage().getMentions().getMentions().get(0).getId()).queue(member -> {
            finalChannel.upsertPermissionOverride(member).setAllowed(perm).queue();
            event.getChannel().sendMessage("> Invite complate!").queue();
        });
    }

    @Override
    public void OnPostCommand(BotInstance instance, GenericEvent e) {

    }
}
