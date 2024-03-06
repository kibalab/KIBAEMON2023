package com.comduck.chatbot.discord.action.commands;

import com.comduck.chatbot.discord.BotInstance;
import com.comduck.chatbot.discord.DiscordBotMain;
import com.comduck.chatbot.discord.action.Command;
import com.comduck.chatbot.discord.action.MessageCommand;
import com.comduck.chatbot.discord.audioV2.PlayerInstance;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.concurrent.atomic.AtomicReference;

//?test
@MessageCommand(name = {"play"}, order = 0)
public class PlayCommand implements Command {

    @Override
    public void OnCommand(BotInstance instance, GenericEvent e, String msg, boolean isUserAction) throws MalformedURLException {

        String video = msg.replaceFirst("play", "").replace(" ", "");

        TextChannel textCh = null;
        VoiceChannel voiceCh = null;
        final Message[] loadMsg = {null};

        MessageReceivedEvent msgEvent = (MessageReceivedEvent) e;
        textCh = msgEvent.getChannel().asTextChannel();
        if(instance == null) instance = new BotInstance(msgEvent.getGuild(), DiscordBotMain.spotifyApi);
        if(!msgEvent.getGuild().getAudioManager().isConnected() && msgEvent.getMember().getVoiceState().getChannel() != null) voiceCh = msgEvent.getMember().getVoiceState().getChannel().asVoiceChannel();

        if(!isUserAction) {
            msgEvent.getMessage().delete().queue();
            textCh.sendMessage("> 불러오는 중").queue(_loadMsg -> {
                loadMsg[0] = _loadMsg;
            });
        }

        try {
            if(instance.playerInstance == null) instance.playerInstance = new PlayerInstance();
            instance.playerInstance.PlayTrackTo(e, textCh, voiceCh, video, (track) -> {
                if(loadMsg[0] != null) loadMsg[0].delete().queue();
            });
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (ParseException ex) {
            ex.printStackTrace();
        } catch (SpotifyWebApiException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void OnPostCommand(BotInstance instance, GenericEvent e) {

    }
}
