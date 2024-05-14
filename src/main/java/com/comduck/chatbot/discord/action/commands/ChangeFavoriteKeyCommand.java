package com.comduck.chatbot.discord.action.commands;

import com.comduck.chatbot.discord.BotInstance;
import com.comduck.chatbot.discord.action.Category;
import com.comduck.chatbot.discord.action.Command;
import com.comduck.chatbot.discord.action.MessageCommand;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@MessageCommand(name = {"change"}, parm = {"FavoriteKey"}, desc = "기존 즐겨찾기에 등록된 곡의 이름을 변경합니다.", cat= Category.Audio)
public class ChangeFavoriteKeyCommand implements Command {

    private static final String msg_ChangeKeyQuery = "UPDATE FavoriteVideo SET Key=\"%s\" WHERE Key=\"%s\";";

    @Override
    public void OnCommand(BotInstance instance, GenericEvent e, String msg, boolean isAdd) {
        MessageReceivedEvent event = (MessageReceivedEvent) e;

        msg = msg.replace("favorite", "");
        String[] arg = msg.split(" ");
        String Key = arg[1];
        String newKey = arg[2];

        event.getChannel().sendMessage(String.format("> 즐겨찾기 별명 변경 ``%s -> %s`` ``%s``", Key, newKey, event.getAuthor().getName())).queue();
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:log.db");
            PreparedStatement preparedStatement = connection.prepareStatement(String.format(msg_ChangeKeyQuery, newKey, Key));
            preparedStatement.executeUpdate();
            connection.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void OnPostCommand(BotInstance instance, GenericEvent e) {

    }
}
