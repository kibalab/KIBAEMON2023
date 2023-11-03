package com.comduck.chatbot.discord.commands;

import com.comduck.chatbot.discord.BotInstance;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.io.File;

@MessageCommand(name = "leave")
public class LeaveCommand implements Command{

    @Override
    public void OnCommand(BotInstance instance, GenericEvent e, String msg) {
        GenericMessageEvent event = (GenericMessageEvent) e;

        VoiceChannel Vch = null;

        if (e instanceof MessageReceivedEvent) {
            Vch = ((MessageReceivedEvent) e).getMember().getVoiceState().getChannel();
            event.getChannel().sendMessage(String.format(
                    "> %s 퇴장 ``%s``",
                    Vch.getName(),
                    ((MessageReceivedEvent) e).getAuthor().getName()
            )).queue();
        }
        event.getGuild().getAudioManager().closeAudioConnection();

        //stopCommand와 같은 부분
        instance.scheduler.getQueue().clear();
        instance.player.stopTrack();
        instance.player.setPaused(false);
    }

    @Override
    public void OnPostCommand(BotInstance instance, GenericEvent e) {

    }
}
