package com.comduck.chatbot.discord;

import com.comduck.chatbot.discord.audiocore.GuildMusicManager;
import com.comduck.chatbot.discord.audiocore.PlayerManager;
import com.comduck.chatbot.discord.audiocore.PostCommandListener;
import com.comduck.chatbot.discord.audiocore.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.container.MediaContainerDescriptor;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.ProbingAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.nico.NicoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import se.michaelthelin.spotify.SpotifyApi;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BotInstance {
    static private final HashMap<String, BotInstance> INSTANCES = new HashMap<>();
    final Guild INSTANCE_GUILD;
    public GuildMusicManager musicManager; // = manager.getGuildMusicManager(event.getGuild());
    public AudioPlayer player; // = musicManager.player;
    public TrackScheduler scheduler; // = musicManager.scheduler;
    public SpotifyApi spotifyApi;
    public int globalVolume = 10;

    public BotInstance(Guild guild, SpotifyApi spotifyApi) {
        this.spotifyApi = spotifyApi;
        this.INSTANCE_GUILD = guild;
        CreateGuildAudioPlayer();
        INSTANCES.put(guild.getId(), this);
    }

    private void CreateGuildAudioPlayer()
    {
        PlayerManager manager = PlayerManager.getInstance();

        manager.playerManager.registerSourceManager(new YoutubeAudioSourceManager());
        manager.playerManager.registerSourceManager(new BandcampAudioSourceManager());
        manager.playerManager.registerSourceManager(new HttpAudioSourceManager());
        manager.playerManager.registerSourceManager(SoundCloudAudioSourceManager.createDefault());
        manager.playerManager.registerSourceManager(new NicoAudioSourceManager("kjh030529@gmail.com", "and7song&"));

        this.musicManager = manager.getGuildMusicManager(INSTANCE_GUILD);
        this.player = musicManager.player;
        this.scheduler = musicManager.scheduler;
    }

    static public BotInstance getInstance(String guildId) {
        return INSTANCES.get(guildId);
    }
}
