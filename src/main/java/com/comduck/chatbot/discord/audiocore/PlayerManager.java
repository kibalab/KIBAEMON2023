package com.comduck.chatbot.discord.audiocore;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.events.message.GenericMessageEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class PlayerManager {
    private static PlayerManager INSTANCE;
    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;

    private PlayerManager() {
        this.musicManagers = new HashMap<Long, GuildMusicManager>();

        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    //
    public synchronized GuildMusicManager getGuildMusicManager(Guild guild) {
        long guildId = guild.getIdLong();
        GuildMusicManager musicManager = musicManagers.get(guildId);

        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager);
            musicManagers.put(guildId, musicManager);
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }


    private void loadAndPlay_Msg(MessageReceivedEvent event, String trackUrl) {


        final GuildMusicManager musicManager = getGuildMusicManager(event.getGuild());

        //트랙 로드, 에러 관련 처리/이벤트
        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            public void trackLoaded(AudioTrack track) {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setColor(new Color(0x244aff));
                eb.addField(track.getInfo().title, String.format("곡이 대기열에 추가되었습니다.\n``%s``", event.getAuthor().getName()), false);
                event.getChannel().sendMessage(eb.build()).queue();

                play(musicManager, track);
            }

            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                AudioTrack firstTrack = audioPlaylist.getSelectedTrack();

                if (firstTrack == null) {
                    firstTrack = audioPlaylist.getTracks().get(0);
                }

                EmbedBuilder eb = new EmbedBuilder();
                eb.setColor(new Color(0x244aff));
                eb.addField(
                        String.format("첫곡 : %s", firstTrack.getInfo().title),
                        String.format(
                                "리스트가 대기열에 추가되었습니다.\n%s\n``%s``",
                                audioPlaylist.getName(),
                                event.getAuthor().getName()
                        ), false);
                event.getChannel().sendMessage(eb.build()).queue();

                play(musicManager, firstTrack);
            }

            public void noMatches() {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setColor(new Color(0xff3e3e));
                eb.addField(trackUrl, String.format("곡을 찾을수 없습니다.\n``%s``", event.getAuthor().getName()), false);
                event.getChannel().sendMessage(eb.build()).queue();
            }

            public void loadFailed(FriendlyException e) {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setColor(new Color(0xff3e3e));
                eb.addField(trackUrl, String.format("곡을 찾을수 없습니다.\n``%s``", event.getAuthor().getName()), false);
                event.getChannel().sendMessage(eb.build()).queue();
                event.getChannel().sendMessage("> " + e.toString()).queue();
            }
        });
    }

    private void loadAndPlay_Reaction(GenericMessageReactionEvent event, String trackUrl) {

        final GuildMusicManager musicManager = getGuildMusicManager(event.getGuild());

        //트랙 로드, 에러 관련 처리/이벤트
        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            public void trackLoaded(AudioTrack track) {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setColor(new Color(0x244aff));
                eb.addField(track.getInfo().title, String.format("곡이 대기열에 추가되었습니다.\n``%s``", event.getUser().getName()), false);
                event.getChannel().sendMessage(eb.build()).queue();

        play(musicManager, track);
    }

    public void playlistLoaded(AudioPlaylist audioPlaylist) {
        AudioTrack firstTrack = audioPlaylist.getSelectedTrack();

        if (firstTrack == null) {
            firstTrack = audioPlaylist.getTracks().get(0);
        }

        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(new Color(0x244aff));
        eb.addField(
                String.format("첫곡 : %s", firstTrack.getInfo().title),
                String.format(
                        "리스트가 대기열에 추가되었습니다.\n%s\n``%s``",
                        audioPlaylist.getName(),
                        event.getUser().getName()
                ), false);
        event.getChannel().sendMessage(eb.build()).queue();

        play(musicManager, firstTrack);
    }

    public void noMatches() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(new Color(0xff3e3e));
        eb.addField(trackUrl, String.format("곡을 찾을수 없습니다.\n``%s``", event.getUser().getName()), false);
        event.getChannel().sendMessage(eb.build()).queue();
    }

    public void loadFailed(FriendlyException e) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(new Color(0xff3e3e));
        eb.addField(trackUrl, String.format("곡을 찾을수 없습니다.\n``%s``", event.getUser().getName()), false);
        event.getChannel().sendMessage(eb.build()).queue();
        event.getChannel().sendMessage("> " + e.toString()).queue();
    }
});
        }

/**
 * 트랙 로드
 *
 * @param event
 * @param trackUrl
 */
public void loadAndPlay(final GenericMessageEvent event, final String trackUrl) {
    if (event instanceof MessageReceivedEvent) {
        loadAndPlay_Msg((MessageReceivedEvent)event, trackUrl);
    } else if (event instanceof GenericMessageReactionEvent) {
        loadAndPlay_Reaction((GenericMessageReactionEvent)event, trackUrl); // 삭제 예정
    }
}

/**
 * 큐에 넘기기
 *
 * @param musicManager
 * @param track
 */
private void play(GuildMusicManager musicManager, AudioTrack track) {
        track.stop();
        musicManager.scheduler.queue(track);
    }

    /**
     * 스크립트 전체 인스턴스로 반환
     *
     * @return
     */
    public static synchronized PlayerManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PlayerManager();
        }
        return INSTANCE;
    }
}