package com.comduck.chatbot.discord.action.commands;

import com.comduck.chatbot.discord.BotInstance;
import com.comduck.chatbot.discord.action.Command;
import com.comduck.chatbot.discord.action.MessageCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;

import java.awt.*;

@MessageCommand(name = {"skip", "next"})
public class SkipCommand implements Command {

    @Override
    public void OnCommand(BotInstance instance, GenericEvent e, String msg) {
        GenericMessageEvent genEvent = (GenericMessageEvent) e;

        //재생되고 있는 트랙이 있는지 확인
        if (instance.player.getPlayingTrack() == null) {
            return;
        }

        // Stop과 같은 처리구조
        if (e instanceof MessageReceivedEvent) {
            MessageReceivedEvent msgEvent = (MessageReceivedEvent) e;
            if (instance.player.getPlayingTrack() == null) {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setColor(new Color(0xff6624));
                eb.addField("경고 Warning", String.format(
                        "대기열이 비어 있습니다.\n``%s``",
                        ((MessageReceivedEvent) e).getAuthor().getName().toString()
                ), false);
                genEvent.getChannel().sendMessage(eb.build()).queue();
            } else {
                genEvent.getChannel().sendMessage(String.format(
                        "> 곡 스킵 ``%s``",
                        ((MessageReceivedEvent) e).getAuthor().getName()
                )).queue();
                instance.scheduler.nextTrack();
            }
        } else if (e instanceof GenericMessageReactionEvent) {
            GenericMessageReactionEvent reactionEvent = (GenericMessageReactionEvent) e;
            if (instance.player.getPlayingTrack() == null) {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setColor(new Color(0xff6624));
                eb.addField("경고 Warning", String.format(
                        "대기열이 비어 있습니다.\n``%s``",
                        ((GenericMessageReactionEvent) e).getUser().getName().toString()
                ), false);
                genEvent.getChannel().sendMessage(eb.build()).queue();
            } else {
                genEvent.getChannel().sendMessage(String.format(
                        "> 곡 스킵 ``%s``",
                        ((GenericMessageReactionEvent) e).getUser().getName()
                )).queue();
                instance.scheduler.nextTrack();
            }
        }
    }

    @Override
    public void OnPostCommand(BotInstance instance, GenericEvent e) {

    }
}
