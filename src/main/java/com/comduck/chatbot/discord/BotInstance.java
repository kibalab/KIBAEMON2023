package com.comduck.chatbot.discord;

import com.comduck.chatbot.discord.audiocore.GuildMusicManager;
import com.comduck.chatbot.discord.audiocore.PostCommandListener;
import com.comduck.chatbot.discord.audiocore.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;

import java.util.ArrayList;
import java.util.List;

public class BotInstance {
    public GuildMusicManager musicManager; // = manager.getGuildMusicManager(event.getGuild());
    public AudioPlayer player; // = musicManager.player;
    public TrackScheduler scheduler; // = musicManager.scheduler;
    public List<PostCommandListener> postCommandListeners;
    public int globalVolume = 10;

    public BotInstance(GuildMusicManager guildMusicManager, AudioPlayer player, TrackScheduler scheduler) {
        this.musicManager = guildMusicManager;
        this.player = player;
        this.scheduler = scheduler;
        this.postCommandListeners = new ArrayList<>();
    }

    // 커맨드 실행 기록 이벤트의 리스너 생성
    public void addPostCommandListener(PostCommandListener listener) {
        this.postCommandListeners.add(listener);
    }

    // 커맨드 실행 기록 이벤트의 리스너 삭제
    public void removePostCommandListener(PostCommandListener listener) {
        this.postCommandListeners.remove(listener);
    }

    // 커맨드 실행 기록 이벤트 발생
    public void raisePostCommand(GenericMessageEvent event) {
        for (PostCommandListener listener : postCommandListeners) {
            listener.onPostCommand(event);
        }
    }
}
