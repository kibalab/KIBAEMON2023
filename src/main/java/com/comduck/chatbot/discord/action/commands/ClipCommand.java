package com.comduck.chatbot.discord.action.commands;

import com.comduck.chatbot.discord.BotInstance;
import com.comduck.chatbot.discord.action.Command;
import com.comduck.chatbot.discord.action.MessageCommand;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.File;
import java.io.IOException;

@MessageCommand(name = {"clip"})
public class ClipCommand implements Command {

    @Override
    public void OnCommand(BotInstance instance, GenericEvent e, String msg, boolean isAdd) throws IOException, ParseException, SpotifyWebApiException {
        MessageReceivedEvent event = (MessageReceivedEvent) e;

        msg = msg.replace("clip ", "");

        VoiceChannel Vch = null;
        AudioManager audiomng = event.getGuild().getAudioManager();
        String url = msg.replaceFirst("play", "").replace(" ", "");

        // GenericMessageEvent 종속 메소드 분리
        if (event instanceof MessageReceivedEvent) {
            Vch = event.getMember().getVoiceState().getChannel().asVoiceChannel();
        } else {
            GenericMessageReactionEvent reactionEvent = (GenericMessageReactionEvent) e;
            Vch = reactionEvent.getMember().getVoiceState().getChannel().asVoiceChannel();
        }

        if (!audiomng.isConnected()) {
            audiomng.openAudioConnection(Vch);
        }


        File clip = new File( "AudioClips/" + msg + ".mp3");
        if(clip.exists()){
            System.out.println("[CommandManager] Play Clip : " + clip.getAbsolutePath());
            instance.playerInstance.PlayTrackTo(event, event.getChannel().asTextChannel(), null, clip.getAbsolutePath(), null);

            event.getChannel().sendMessage("> " + msg + " 시작!").queue();
        }
    }

    @Override
    public void OnPostCommand(BotInstance instance, GenericEvent e) {

    }
}
