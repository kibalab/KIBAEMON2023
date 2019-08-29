package com.comduck.chatbot.discord.audiocore;

import net.dv8tion.jda.core.events.message.GenericMessageEvent;

public interface PostCommandListener {
    void onPostCommand(GenericMessageEvent genericMessageEvent);
}
