package com.comduck.chatbot.discord.action.commands;

import com.comduck.chatbot.discord.BotInstance;
import com.comduck.chatbot.discord.action.Command;
import com.comduck.chatbot.discord.action.MessageCommand;
import com.comduck.chatbot.discord.action.commands.util.TimeUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;

import java.awt.*;

@MessageCommand(name = {"goto"})
public class GotoCommand implements Command {

    @Override
    public void OnCommand(BotInstance instance, GenericEvent e, String msg, boolean isAdd) {
        GenericMessageEvent genEvent = (GenericMessageEvent) e;

        msg = msg.replaceFirst("goto ", "");

        //재생되고 있는 트랙이 있는지 확인
        if (instance.player.getPlayingTrack() == null) {
            EmbedBuilder eb = new EmbedBuilder();
            eb.setColor(new Color(0xff6624));
            if (e instanceof MessageReceivedEvent) {
                MessageReceivedEvent msgEvent = (MessageReceivedEvent) e;
                eb.addField("경고 Warning", String.format(
                        "대기열이 비어 있습니다.\n``%s``",
                        msgEvent.getAuthor().getName()
                ), false);
            } else if (e instanceof GenericMessageReactionEvent) {
                GenericMessageReactionEvent reactionEvent = (GenericMessageReactionEvent) e;
                eb.addField("경고 Warning", String.format(
                        "대기열이 비어 있습니다.\n``%s``",
                        reactionEvent.getUser().getName()
                ), false);
            }
            genEvent.getChannel().sendMessage(eb.build()).queue();
        } else { //비어있지 않으면

            //파라미터(시간 문자열)를 프레임단위로 환산하여 Position에 넣음
            long time = TimeUtil.formatLong(msg);
            System.out.println(time);
            instance.player.getPlayingTrack().setPosition(time);

        }
    }

    @Override
    public void OnPostCommand(BotInstance instance, GenericEvent e) {

    }
}
