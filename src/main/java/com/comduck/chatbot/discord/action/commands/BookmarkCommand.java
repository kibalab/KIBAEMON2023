package com.comduck.chatbot.discord.action.commands;

import com.comduck.chatbot.database.MariaDB;
import com.comduck.chatbot.database.Table;
import com.comduck.chatbot.discord.BotInstance;
import com.comduck.chatbot.discord.action.Category;
import com.comduck.chatbot.discord.action.Command;
import com.comduck.chatbot.discord.action.MessageCommand;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.SQLException;
import java.util.HashMap;

@MessageCommand(name = "bookmark", parm = {"CategoryID"}, desc = "서버내에 개인 북마크 채널을 생성할 채널카테고리를 등록합니다.", cat= Category.Social)
public class BookmarkCommand implements Command {
    @Override
    public void OnCommand(BotInstance instance, GenericEvent e, String msg, boolean isAdd) throws ClassNotFoundException {
        MessageReceivedEvent event = (MessageReceivedEvent) e;
        String catid = msg.replaceFirst("bookmark", "").replace(" ", "");

        try {
            if(!MariaDB.Get(Table.SERVER_SETTINGS, "server_id", event.getGuild().getId()).next())
            {
                HashMap<String, Object> data = new HashMap<>();
                data.put("server_id", event.getGuild().getId());
                data.put("bm_cat_id", catid);
                MariaDB.Add(Table.SERVER_SETTINGS, data);
                event.getChannel().sendMessage("> Set Private Bookmark Category").queue();
            }
            else{
                MariaDB.Set(Table.SERVER_SETTINGS, "server_id", event.getGuild().getId(), "bm_cat_id", catid);
                event.getChannel().sendMessage("> Change Private Bookmark Category").queue();
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
