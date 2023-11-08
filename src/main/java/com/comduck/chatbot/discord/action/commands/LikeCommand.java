package com.comduck.chatbot.discord.action.commands;

import com.comduck.chatbot.database.MariaDB;
import com.comduck.chatbot.database.Table;
import com.comduck.chatbot.discord.BotInstance;
import com.comduck.chatbot.discord.action.Command;
import com.comduck.chatbot.discord.action.MessageCommand;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.SQLException;
import java.util.HashMap;

@MessageCommand(name = "like")
public class LikeCommand implements Command {
    @Override
    public void OnCommand(BotInstance instance, GenericEvent e, String msg, boolean isAdd) throws SQLException, ClassNotFoundException {
        MessageReceivedEvent event = (MessageReceivedEvent) e;
        String catid = msg.replaceFirst("like", "").replace(" ", "");

        try {
            if(!MariaDB.Get(Table.SERVER_SETTINGS, "server_id", event.getGuild().getId()).next())
            {
                HashMap<String, Object> data = new HashMap<>();
                data.put("server_id", event.getGuild().getId());
                data.put("like_ch_id", catid);
                MariaDB.Add(Table.SERVER_SETTINGS, data);
                event.getChannel().sendMessage("> Set Public Like Channel").queue();
            }
            else{
                MariaDB.Set(Table.SERVER_SETTINGS, "server_id", event.getGuild().getId(), "like_ch_id", catid);
                event.getChannel().sendMessage("> Set Public Like Channel").queue();
            }
        } catch (SQLException ex) {
            event.getChannel().sendMessage("> Fail").queue();
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void OnPostCommand(BotInstance instance, GenericEvent e) {

    }
}
