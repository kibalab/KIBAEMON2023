package com.comduck.chatbot.discord.action.commands;

import com.comduck.chatbot.discord.BotInstance;
import com.comduck.chatbot.discord.action.Command;
import com.comduck.chatbot.discord.action.MessageCommand;
import com.comduck.chatbot.discord.api.naverapi.Shopping;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@MessageCommand(name = {"shopping", "shop"})
public class ShoppingCommand implements Command {

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
