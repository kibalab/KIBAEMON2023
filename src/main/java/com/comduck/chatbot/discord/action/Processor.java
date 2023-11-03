package com.comduck.chatbot.discord.action;

import com.comduck.chatbot.discord.BotInstance;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;

public interface Processor {
    public void OnProcess(GenericMessageEvent e, String msg);
}
