package com.comduck.chatbot.discord.action.commands;

import com.comduck.chatbot.discord.BotInstance;
import com.comduck.chatbot.discord.DiscordBotMain;
import com.comduck.chatbot.discord.action.Category;
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
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

//?play
@MessageCommand(name = {"play", "재생", "p"}, parm = {"VideoUrl"}, order = 0, desc = "음악을 재생합니다", cat = Category.Audio)
public class PlayCommand implements Command {

    @Override
    public void OnCommand(BotInstance instance, GenericEvent e, String msg, boolean isUserAction) throws MalformedURLException {

        //TODO: 파일로 재생하는 경우, 파일을 다운로드 받아 재생하는 기능 추가

        TextChannel textCh = null;
        VoiceChannel voiceCh = null;
        final Message[] loadMsg = {null};

        MessageReceivedEvent msgEvent = (MessageReceivedEvent) e;
        textCh = msgEvent.getChannel().asTextChannel();
        if (instance == null) instance = new BotInstance(msgEvent.getGuild(), DiscordBotMain.spotifyApi);
        if (!msgEvent.getGuild().getAudioManager().isConnected() && msgEvent.getMember().getVoiceState().getChannel() != null)
            voiceCh = msgEvent.getMember().getVoiceState().getChannel().asVoiceChannel();

        String video = msg.replaceFirst("play", "").replace(" ", "");
        boolean requireRemove = true;

        if (video.isBlank() || video.isEmpty()) {
            var attachments = msgEvent.getMessage().getAttachments();

            if (attachments.size() > 0) {
                video = attachments.get(0).getUrl();
                requireRemove = false;
            } else {
                textCh.sendMessage("URL을 입력해주세요.").queue();
            }
        }
        System.out.println(video);

        if (!isUserAction && requireRemove) {
            msgEvent.getMessage().delete().queue();
            textCh.sendMessage("> 불러오는 중").queue(_loadMsg -> {
                loadMsg[0] = _loadMsg;
            });
        }

        try {
            if (instance.playerInstance == null) instance.playerInstance = new PlayerInstance();
            instance.playerInstance.PlayTrackTo(e, textCh, voiceCh, video, (track) -> {
                if (loadMsg[0] != null) loadMsg[0].delete().queue();
            });
        } catch (IOException | ParseException | SpotifyWebApiException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void OnPostCommand(BotInstance instance, GenericEvent e) {

    }
}
