package com.comduck.chatbot.discord.action.commands;

import com.comduck.chatbot.discord.BotInstance;
import com.comduck.chatbot.discord.action.Command;
import com.comduck.chatbot.discord.action.MessageCommand;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;

@MessageCommand(name = {"stop"})
public class StopCommand implements Command {

    @Override
    public void OnCommand(BotInstance instance, GenericEvent e, String msg) {
        GenericMessageEvent genEvent = (GenericMessageEvent) e;

        //재생되고 있는 트랙이 있는지 확인
        if (instance.player.getPlayingTrack() == null) {
            return;
        }

        //GenericMessageEvent 종속 메소드 분리후 처리
        if (e instanceof MessageReceivedEvent) {
            MessageReceivedEvent msgEvent = (MessageReceivedEvent) e;

            msgEvent.getChannel().sendMessage(String.format(
                    "> 대기열 재생 중지 ``%s``",
                    msgEvent.getAuthor().getName()
            )).queue();
        } else if (e instanceof GenericMessageReactionEvent) {
            GenericMessageReactionEvent reactionEvent = (GenericMessageReactionEvent) e;

            reactionEvent.getChannel().sendMessage(String.format(
                    "> 대기열 재생 중지 ``%s``",
                    reactionEvent.getUser().getName()
            )).queue();
        }

        //1. tracklist를 초기화
        //2. 현재 재생되고 있는 트랙 정지
        //3. 플레이어 일시정지
        instance.scheduler.getQueue().clear();
        instance.player.stopTrack();
        instance.player.setPaused(false);

        //커맨드 실행 기록 이벤트 발생
        instance.raisePostCommand(genEvent);
    }

    @Override
    public void OnPostCommand(BotInstance instance, GenericEvent e) {

    }
}
