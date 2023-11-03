package com.comduck.chatbot.discord.action.commands;

import com.comduck.chatbot.discord.BotInstance;
import com.comduck.chatbot.discord.action.Command;
import com.comduck.chatbot.discord.action.MessageCommand;
import com.comduck.chatbot.discord.minigame.Roulette;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.Arrays;

@MessageCommand(name = {"roulette", "rol"})
public class RouletteCommand implements Command {

    static Roulette roulette = new Roulette();

    @Override
    public void OnCommand(BotInstance instance, GenericEvent e, String msg) {
        MessageReceivedEvent event = (MessageReceivedEvent) e;

        msg = msg.replace("roulette ", "").replace("rol ", "");
        roulette.betting(event.getAuthor(), Integer.parseInt(msg));
        String[] r = roulette.letRoulette(event.getAuthor());
        int f = roulette.checkResult(event.getAuthor(), r);

        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(new Color(0xCF00FF));
        eb.setAuthor("Roulette");
        DecimalFormat form = new DecimalFormat("###,###,###,###,###,###,###,###,###,###,###");
        String rs = Arrays.toString(r).replaceAll("\\[", "").replaceAll("]", "");
        eb.addField(String.format("[%s] - %dx", rs, f), String.format("현재 금액 : %s", form.format(roulette.getMoney(event.getAuthor()))), false);
        event.getChannel().sendMessage(eb.build()).queue();
    }

    @Override
    public void OnPostCommand(BotInstance instance, GenericEvent e) {

    }
}
