package com.comduck.chatbot.discord.commands;

import com.comduck.chatbot.discord.commands.MessageCommand;
import net.dv8tion.jda.api.events.GenericEvent;

public interface Command {
    @MessageCommand
    public void OnCommand(GenericEvent event, String msg);

    public void OnPostCommand(GenericEvent event);
}
