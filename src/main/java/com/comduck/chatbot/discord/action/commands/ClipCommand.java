package com.comduck.chatbot.discord.action.commands;

import com.comduck.chatbot.discord.BotInstance;
import com.comduck.chatbot.discord.action.Command;
import com.comduck.chatbot.discord.action.MessageCommand;
import com.comduck.chatbot.discord.audiocore.PlayerManager;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.managers.AudioManager;

import java.io.File;

@MessageCommand(name = {"clip"})
public class ClipCommand implements Command {

    @Override
    public void OnCommand(BotInstance instance, GenericEvent e, String msg) {
        MessageReceivedEvent event = (MessageReceivedEvent) e;

        msg = msg.replace("clip ", "");

        VoiceChannel Vch = null;
        PlayerManager manager = PlayerManager.getInstance();
        AudioManager audiomng = event.getGuild().getAudioManager();
        String url = msg.replaceFirst("play", "").replace(" ", "");

        // GenericMessageEvent 종속 메소드 분리
        if (event instanceof MessageReceivedEvent) {
            Vch = event.getMember().getVoiceState().getChannel();
        } else {
            GenericMessageReactionEvent reactionEvent = (GenericMessageReactionEvent) e;
            Vch = reactionEvent.getMember().getVoiceState().getChannel();
        }

        if (!audiomng.isConnected()) {
            audiomng.openAudioConnection(Vch);
        }


        File clip = new File( "AudioClips/" + msg + ".mp3");
        if(clip.exists()){
            System.out.println("[CommandManager] Play Clip : " + clip.getAbsolutePath());
            instance.raisePostCommand(event);
            PlayerManager playerManager = PlayerManager.getInstance();
            playerManager.loadAndPlay(event, clip.getAbsolutePath());
            playerManager.getGuildMusicManager(event.getGuild()).player.setVolume(instance.globalVolume * 2);

            event.getChannel().sendMessage("> " + msg + " 시작!").queue();
        }
    }

    @Override
    public void OnPostCommand(BotInstance instance, GenericEvent e) {

    }
}
