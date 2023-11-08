package com.comduck.chatbot.discord.action;

import com.comduck.chatbot.discord.BotInstance;
import net.dv8tion.jda.api.events.GenericEvent;

import java.sql.SQLException;

public interface Command {

    @MessageCommand
    public void OnCommand(BotInstance instance, GenericEvent e, String msg, boolean isAdd) throws SQLException, ClassNotFoundException;

    public void OnPostCommand(BotInstance instance, GenericEvent e);
}
