package com.comduck.chatbot.discord.action.commands;

import com.comduck.chatbot.discord.BotInstance;
import com.comduck.chatbot.discord.action.Command;
import com.comduck.chatbot.discord.action.MessageCommand;
import com.comduck.chatbot.discord.action.commands.db.UpdateDB;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@MessageCommand(name = {"PlayingDisplay"})
public class PlayingDisplayCommand implements Command {

    @Override
    public void OnCommand(BotInstance instance, GenericEvent e, String msg, boolean isAdd) {
        MessageReceivedEvent event = (MessageReceivedEvent) e;

        msg = msg.replaceFirst("PlayingDisplay ", "");
        UpdateDB.UpdateServerIndex(event, "PlayDisplay", msg);
    }

    @Override
    public void OnPostCommand(BotInstance instance, GenericEvent e) {

    }
}
