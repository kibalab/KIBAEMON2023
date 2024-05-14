package com.comduck.chatbot.discord.action.commands;

import com.comduck.chatbot.discord.BotInstance;
import com.comduck.chatbot.discord.action.Category;
import com.comduck.chatbot.discord.action.Command;
import com.comduck.chatbot.discord.action.MessageCommand;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@MessageCommand(name = {"leave", "out"}, desc = "음성채널을 나갑니다.", cat = Category.Audio)
public class LeaveCommand implements Command {

    @Override
    public void OnCommand(BotInstance instance, GenericEvent e, String msg, boolean isAdd) {
        GenericMessageEvent event = (GenericMessageEvent) e;

        VoiceChannel Vch = null;

        if (e instanceof MessageReceivedEvent) {
            Vch = ((MessageReceivedEvent) e).getMember().getVoiceState().getChannel().asVoiceChannel();
            event.getChannel().sendMessage(String.format(
                    "> %s 퇴장 ``%s``",
                    Vch.getName(),
                    ((MessageReceivedEvent) e).getAuthor().getName()
            )).queue();
        }
        event.getGuild().getAudioManager().closeAudioConnection();

        //stopCommand와 같은 부분
        instance.playerInstance.trackScheduler.clear();
        instance.playerInstance.player.stopTrack();
        instance.playerInstance.player.setPaused(false);
    }

    @Override
    public void OnPostCommand(BotInstance instance, GenericEvent e) {

    }
}
