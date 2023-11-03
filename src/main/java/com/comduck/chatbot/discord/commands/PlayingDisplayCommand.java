package com.comduck.chatbot.discord.commands;

import com.comduck.chatbot.discord.BotInstance;
import com.comduck.chatbot.discord.commands.db.UpdateDB;
import com.comduck.chatbot.discord.minigame.Roulette;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@MessageCommand(name = "playingdisplay")
public class PlayingDisplayCommand implements Command{

    @Override
    public void OnCommand(BotInstance instance, GenericEvent e, String msg) {
        MessageReceivedEvent event = (MessageReceivedEvent) e;

        msg = msg.replaceFirst("PlayingDisplay ", "");
        UpdateDB.UpdateServerIndex(event, "PlayDisplay", msg);
    }

    @Override
    public void OnPostCommand(BotInstance instance, GenericEvent e) {

    }
}
