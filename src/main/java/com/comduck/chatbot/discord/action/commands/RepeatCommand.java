package com.comduck.chatbot.discord.action.commands;

import com.comduck.chatbot.discord.BotInstance;
import com.comduck.chatbot.discord.CommandManager;
import com.comduck.chatbot.discord.action.Command;
import com.comduck.chatbot.discord.action.MessageCommand;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;

@MessageCommand(name = {"repeat", "replay", "rp"})
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
            CommandManager.ExcuteMessageCommend("play", msgEvent, msg);
            msgEvent.getChannel().sendMessage(String.format("> 현재곡 재등록 ``%s``", ((MessageReceivedEvent) e).getAuthor().getName())).queue();
        } else if (e instanceof GenericMessageReactionEvent) {
            reactionEvent = (GenericMessageReactionEvent) e;
            CommandManager.ExcuteMessageCommend("play", reactionEvent, msg);
            reactionEvent.getChannel().sendMessage(String.format("> 현재곡 재등록 ``%s``", ((GenericMessageReactionEvent) e).getUser().getName())).queue();
        } else if (e instanceof ButtonInteractionEvent) {
            buttonEvent = (ButtonInteractionEvent) e;
            CommandManager.ExcuteButtonAction("play", buttonEvent, msg);
            buttonEvent.reply(String.format("> 현재곡 재등록 ``%s``", ((ButtonInteractionEvent) e).getUser().getName())).setEphemeral(true).queue();
        }
    }

    @Override
    public void OnPostCommand(BotInstance instance, GenericEvent e) {

    }
}
