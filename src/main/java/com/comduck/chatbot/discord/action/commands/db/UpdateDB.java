package com.comduck.chatbot.discord.action.commands.db;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class UpdateDB {


    static private final String SvSt_SettingDataQuery = "UPDATE ServerSetting SET %s='%s' WHERE id=%s;";
    /*
    == 서버 세팅 ==
    [1] 서버이름
    [2] 서버아이디
    [3] 재생표시 방법
    [4] 현재서버 재생볼륨
    ==============
     */
    static public void UpdateServerIndex(MessageReceivedEvent event, String fieldName, String data) {
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:log.db");
            PreparedStatement preparedStatement = connection.prepareStatement(String.format(SvSt_SettingDataQuery, fieldName, data, event.getGuild().getId()));
            preparedStatement.executeUpdate();
        } catch (Exception e) {  }
    }
}
