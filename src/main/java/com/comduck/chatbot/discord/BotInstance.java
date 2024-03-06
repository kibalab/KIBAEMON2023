package com.comduck.chatbot.discord;

import com.comduck.chatbot.discord.audioV2.PlayerInstance;
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
    public SpotifyApi spotifyApi;
    public int globalVolume = 10;
    public PlayerInstance playerInstance;

    public BotInstance(Guild guild, SpotifyApi spotifyApi) {
        System.out.println("[BotInstance] Instantiate BotInstance : " + guild.getName());

        this.playerInstance = new PlayerInstance();
        this.spotifyApi = spotifyApi;
        this.INSTANCE_GUILD = guild;
        //CreateGuildAudioPlayer();
        INSTANCES.put(guild.getId(), this);
    }

    static public BotInstance getInstance(String guildId) {
        return INSTANCES.get(guildId);
    }
}
