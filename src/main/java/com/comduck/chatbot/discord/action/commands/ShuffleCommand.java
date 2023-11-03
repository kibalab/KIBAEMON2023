package com.comduck.chatbot.discord.action.commands;

import com.comduck.chatbot.discord.BotInstance;
import com.comduck.chatbot.discord.action.Command;
import com.comduck.chatbot.discord.action.MessageCommand;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

@MessageCommand(name = {"shuffle", "mix", "sf"})
public class ShuffleCommand implements Command {

    @Override
    public void OnCommand(BotInstance instance, GenericEvent e, String msg) {
        GenericMessageEvent genEvent = (GenericMessageEvent) e;

        //재생되고 있는 트랙이 있는지 확인
        if (instance.player.getPlayingTrack() == null) {
            return;
        }

        //큐를 가져옴, 빈 리스트(AudioTrack) 생성
        Queue queue = instance.scheduler.getQueue();
        List<AudioTrack> list = new ArrayList<>();

        //큐에서 하나씩 빼서 리스트에 넣음
        for (int i = 0; true; i++) {
            list.add((AudioTrack) queue.poll());
            if (queue.size() == 0) {
                break;
            }
        }

        //리스트를 무작위로 섞음
        Collections.shuffle(list);

        //다시 큐에 넣음
        for (int i = 0; true; i++) {
            queue.offer(list.get(i));
            if (queue.size() == list.size()) {
                break;
            }
        }

        if (e instanceof MessageReceivedEvent) {
            MessageReceivedEvent msgEvent = (MessageReceivedEvent) e;
            msgEvent.getChannel().sendMessage(String.format("> 대기열 셔플 ``%s``", ((MessageReceivedEvent) e).getAuthor().getName())).queue();
        } else if (e instanceof GenericMessageReactionEvent) {
            GenericMessageReactionEvent reactionEvent = (GenericMessageReactionEvent) e;
            reactionEvent.getChannel().sendMessage(String.format("> 대기열 셔플 ``%s``", ((GenericMessageReactionEvent) e).getUser().getName())).queue();
        }
    }

    @Override
    public void OnPostCommand(BotInstance instance, GenericEvent e) {

    }
}
