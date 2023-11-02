package com.comduck.chatbot.discord.commands;

import net.dv8tion.jda.api.events.GenericEvent;

//?test
@MessageCommand(name = "test", order = 0)
public class TestCommand implements Command {

    @Override
    public void OnCommand(GenericEvent event, String msg) {
        System.out.println("[Test] OnCommand");
    }

    @Override
    public void OnPostCommand(GenericEvent event) {
        System.out.println("[Test] OnPostCommand");
    }
}
