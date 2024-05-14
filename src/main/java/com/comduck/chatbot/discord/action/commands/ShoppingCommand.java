package com.comduck.chatbot.discord.action.commands;

import com.comduck.chatbot.discord.BotInstance;
import com.comduck.chatbot.discord.action.Category;
import com.comduck.chatbot.discord.action.Command;
import com.comduck.chatbot.discord.action.MessageCommand;
import com.comduck.chatbot.discord.api.naverapi.Shopping;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@MessageCommand(name = {"shopping", "shop"}, parm = {"SearchText"}, desc = "네이버 쇼핑에 상품을 검색합니다.", cat= Category.API)
public class ShoppingCommand implements Command {

    @Override
    public void OnCommand(BotInstance instance, GenericEvent e, String msg, boolean isAdd) {
        GenericMessageEvent event = (GenericMessageEvent) e;

        MessageReceivedEvent msgEvent = (MessageReceivedEvent) event;
        Shopping shop = new Shopping();
        EmbedBuilder eb = shop.manager(msg.replace("shopping ", "").replace("shop ", ""));
        event.getChannel().sendMessageEmbeds(eb.build()).queue();
    }

    @Override
    public void OnPostCommand(BotInstance instance, GenericEvent e) {

    }
}
