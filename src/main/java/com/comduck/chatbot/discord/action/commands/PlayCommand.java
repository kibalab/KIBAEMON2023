package com.comduck.chatbot.discord.action.commands;

import com.comduck.chatbot.discord.BotInstance;
import com.comduck.chatbot.discord.action.Command;
import com.comduck.chatbot.discord.action.MessageCommand;
import com.comduck.chatbot.discord.audiocore.PlayerManager;
import com.comduck.chatbot.discord.action.commands.util.WebUtil;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.managers.AudioManager;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//?test
@MessageCommand(name = {"play"}, order = 0)
public class PlayCommand implements Command {

    @Override
    public void OnCommand(BotInstance instance, GenericEvent e, String msg, boolean isAdd) throws MalformedURLException {
        GenericMessageEvent genEvent = (GenericMessageEvent) e;

        VoiceChannel Vch = null;
        PlayerManager manager = PlayerManager.getInstance();
        AudioManager audiomng = genEvent.getGuild().getAudioManager();
        String url = msg.replaceFirst("play", "").replace(" ", "");

        URL link = new URL(url);
        String host = link.getHost();

        // GenericMessageEvent 종속 메소드 분리
        if (e instanceof MessageReceivedEvent) {
            MessageReceivedEvent msgEvent = (MessageReceivedEvent) e;
            Vch = msgEvent.getMember().getVoiceState().getChannel().asVoiceChannel();
        } else {
            GenericMessageReactionEvent reactionEvent = (GenericMessageReactionEvent) e;
            Vch = reactionEvent.getMember().getVoiceState().getChannel().asVoiceChannel();
        }

        if (!audiomng.isConnected()) {
            audiomng.openAudioConnection(Vch);
        }


        //1. 파라미터 없으면 현재 재생되고 있는 음액 재신청
        //2. URL이 아니면 유튜브에 검색
        if(url.equals("")) {
            url = instance.player.getPlayingTrack().getInfo().uri;
        } else if (url.startsWith("<#")){

            TextChannel ch = e.getJDA().getTextChannelById(url.replace("<#", "").replace(">", ""));
            List<Message> msgs = ch.getIterableHistory().complete();
            Collections.shuffle(msgs);
            for(Message m : msgs) {
                if (WebUtil.isURL(m.getContentDisplay()) && m.getContentDisplay().contains("youtu")) {
                    //음악 재생
                    manager.loadAndPlay(genEvent, m.getContentDisplay());
                    manager.getGuildMusicManager(genEvent.getGuild()).player.setVolume(instance.globalVolume);
                }
            }
            return;
        }else {
            //음악 재생
            manager.loadAndPlay(genEvent, url);
            manager.getGuildMusicManager(genEvent.getGuild()).player.setVolume(instance.globalVolume);
            if (WebUtil.isURL(url)) {

            }
        }
    }

    @Override
    public void OnPostCommand(BotInstance instance, GenericEvent e) {

    }
}
