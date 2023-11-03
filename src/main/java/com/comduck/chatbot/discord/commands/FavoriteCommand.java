package com.comduck.chatbot.discord.commands;

import com.comduck.chatbot.discord.BotInstance;
import com.comduck.chatbot.discord.audiocore.PlayerManager;
import com.comduck.chatbot.discord.commands.db.UpdateDB;
import com.comduck.chatbot.discord.minigame.Roulette;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.managers.AudioManager;

import java.io.File;
import java.sql.*;

@MessageCommand(name = "favorite")
public class FavoriteCommand implements Command{

    private static String Msg_loadFavoriteQuery = "SELECT * FROM FavoriteVideo WHERE Server=%s AND Key=\"%s\";";

    @Override
    public void OnCommand(BotInstance instance, GenericEvent e, String msg) {
        MessageReceivedEvent event = (MessageReceivedEvent) e;

        msg = msg.replace("favorite", "");
        String Key = msg.replaceAll(" ", "");

        event.getChannel().sendMessage(String.format("> 즐겨찾기 재생 ``%s`` ``%s``", Key, event.getAuthor().getName())).queue();
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:log.db");
            Statement st = connection.createStatement();
            String instanceQuery = String.format(Msg_loadFavoriteQuery, event.getGuild().getId(), Key);
            ResultSet result = st.executeQuery(instanceQuery);

            while ( result.next() )
            {
                //playCommand(event, result.getString(2));
            }
            st.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void OnPostCommand(BotInstance instance, GenericEvent e) {

    }
}
