package com.comduck.chatbot.discord.action.commands;

import com.comduck.chatbot.discord.BotInstance;
import com.comduck.chatbot.discord.action.Command;
import com.comduck.chatbot.discord.action.MessageCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.awt.*;

@MessageCommand(name = {"stop"})
public class StopCommand implements Command {

    @Override
    public void OnCommand(BotInstance instance, GenericEvent e, String msg, boolean isAdd) {

        //재생되고 있는 트랙이 있는지 확인
        if (instance.playerInstance.player.getPlayingTrack() == null) {
            return;
        }

        //1. tracklist를 초기화
        //2. 현재 재생되고 있는 트랙 정지
        //3. 플레이어 일시정지
        instance.playerInstance.trackScheduler.clear();
        instance.playerInstance.player.stopTrack();
        instance.playerInstance.player.setPaused(false);

        MessageCreateData resultMsg;
        resultMsg = MessageCreateData.fromContent("> 대기열 재생 중지");

        if (e instanceof MessageReceivedEvent) {
            MessageReceivedEvent msgEvent = (MessageReceivedEvent) e;
            msgEvent.getChannel().sendMessageEmbeds(resultMsg.getEmbeds()).queue();
        } else if (e instanceof GenericMessageReactionEvent) {
            GenericMessageReactionEvent reactionEvent = (GenericMessageReactionEvent) e;
            reactionEvent.getChannel().sendMessageEmbeds(resultMsg.getEmbeds()).queue();
        } else if (e instanceof ButtonInteractionEvent) {
            ButtonInteractionEvent reactionEvent = (ButtonInteractionEvent) e;
            reactionEvent.reply(resultMsg).setEphemeral(true).queue();
        }
    }

    @Override
    public void OnPostCommand(BotInstance instance, GenericEvent e) {

    }
}
