package com.comduck.chatbot.discord.action;

import com.comduck.chatbot.discord.BotInstance;
import net.dv8tion.jda.api.events.GenericEvent;

public interface Command {

    @MessageCommand
    public void OnCommand(BotInstance instance, GenericEvent e, String msg);

    public void OnPostCommand(BotInstance instance, GenericEvent e);
}
