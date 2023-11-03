package com.comduck.chatbot.discord.action.commands;

import com.comduck.chatbot.discord.BotInstance;
import com.comduck.chatbot.discord.action.Command;
import com.comduck.chatbot.discord.action.MessageCommand;
import com.comduck.chatbot.discord.api.naverapi.Papago;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;

@MessageCommand(name = {"papago"})
public class PapagoCommand implements Command {

    @Override
    public void OnCommand(BotInstance instance, GenericEvent e, String msg) {
        GenericMessageEvent event = (GenericMessageEvent) e;

        MessageReceivedEvent msgEvent = (MessageReceivedEvent) e;
        Papago papago = new Papago();
        String[] data = papago.manager(msg.replace("papago ", ""));
        String result = data[0]; String sourceLang = data[1]; String targetLang = data[2];

        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(new Color(0x1FFF2A));
        eb.setAuthor("Papago", "https://papago.naver.com/", "https://papago.naver.com/static/img/papago_og.png");
        eb.addField(String.format("[:flag_%s: -> :flag_%s:]", sourceLang, targetLang), result, false);
        event.getChannel().sendMessage(eb.build()).queue();
    }

    @Override
    public void OnPostCommand(BotInstance instance, GenericEvent e) {

    }
}
