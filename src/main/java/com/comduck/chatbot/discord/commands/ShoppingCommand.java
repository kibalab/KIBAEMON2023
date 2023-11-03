package com.comduck.chatbot.discord.commands;

import com.comduck.chatbot.discord.BotInstance;
import com.comduck.chatbot.discord.naverapi.Shopping;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.File;

@MessageCommand(name = "shopping")
public class ShoppingCommand implements Command{

    @Override
    public void OnCommand(BotInstance instance, GenericEvent e, String msg) {
        GenericMessageEvent event = (GenericMessageEvent) e;

        MessageReceivedEvent msgEvent = (MessageReceivedEvent) event;
        Shopping shop = new Shopping();
        EmbedBuilder eb = shop.manager(msg.replace("shopping ", "").replace("shop ", ""));
        event.getChannel().sendMessage(eb.build()).queue();
    }

    @Override
    public void OnPostCommand(BotInstance instance, GenericEvent e) {

    }
}
