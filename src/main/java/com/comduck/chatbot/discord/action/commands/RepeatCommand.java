package com.comduck.chatbot.discord.action.commands;

import com.comduck.chatbot.discord.BotInstance;
import com.comduck.chatbot.discord.ActionManager;
import com.comduck.chatbot.discord.action.Category;
import com.comduck.chatbot.discord.action.Command;
import com.comduck.chatbot.discord.action.MessageCommand;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;

@MessageCommand(name = {"repeat", "replay", "rp"}, desc = "현재 재생중인 음악을 재신청합니다.", cat = Category.Audio)
public class RepeatCommand implements Command {

    @Override
    public void OnCommand(BotInstance instance, GenericEvent e, String msg, boolean isAdd) {

        //재생되고 있는 트랙이 있는지 확인
        if (instance.playerInstance.player.getPlayingTrack() == null) {
            return;
        }

        MessageReceivedEvent msgEvent = null;
        GenericMessageReactionEvent reactionEvent = null;
        ButtonInteractionEvent buttonEvent = null;

        if (e instanceof MessageReceivedEvent) {
            msgEvent = (MessageReceivedEvent) e;
            ActionManager.ExcuteMessageCommend(msgEvent, "?play " + instance.playerInstance.player.getPlayingTrack().getInfo().uri, false);
            msgEvent.getChannel().sendMessage(String.format("> 현재곡 재등록 ``%s``", ((MessageReceivedEvent) e).getAuthor().getName())).queue();
        }
    }

    @Override
    public void OnPostCommand(BotInstance instance, GenericEvent e) {

    }
}
