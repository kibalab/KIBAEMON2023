package com.comduck.chatbot.discord.action.commands;

import com.comduck.chatbot.discord.BotInstance;
import com.comduck.chatbot.discord.CommandManager;
import com.comduck.chatbot.discord.action.Command;
import com.comduck.chatbot.discord.action.MessageCommand;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;

@MessageCommand(name = {"repeat", "replay", "rp"})
public class RepeatCommand implements Command {

    @Override
    public void OnCommand(BotInstance instance, GenericEvent e, String msg) {
        GenericMessageEvent genEvent = (GenericMessageEvent) e;

        //재생되고 있는 트랙이 있는지 확인
        if (instance.player.getPlayingTrack() == null) {
            return;
        }

        //1. 현재 재생되고 있는 트랙의 url정보를 가져옴
        //2. url 정보를 커맨드 메시지처럼 사용하기 위해 수정함
        //3. playCommand 호출

        CommandManager.ExcuteMessageCommend("play", genEvent, msg);

        msg = "play " + instance.player.getPlayingTrack().getInfo().uri;

        if (e instanceof MessageReceivedEvent) {
            MessageReceivedEvent msgEvent = (MessageReceivedEvent) e;
            msgEvent.getChannel().sendMessage(String.format("> 현재곡 재등록 ``%s``", ((MessageReceivedEvent) e).getAuthor().getName())).queue();
        } else if (e instanceof GenericMessageReactionEvent) {
            GenericMessageReactionEvent reactionEvent = (GenericMessageReactionEvent) e;
            reactionEvent.getChannel().sendMessage(String.format("> 현재곡 재등록 ``%s``", ((GenericMessageReactionEvent) e).getUser().getName())).queue();
        }
    }

    @Override
    public void OnPostCommand(BotInstance instance, GenericEvent e) {

    }
}
