package com.comduck.chatbot.discord.audioV2;

import com.comduck.chatbot.discord.audiocore.YoutubeParse;
import com.comduck.chatbot.discord.audiocore.enums.Platform;
import com.comduck.chatbot.discord.audiocore.imgproc.YoutubePlayingImageProcessor;
import com.comduck.chatbot.discord.audiocore.webutil.SpotifyWebUtil;
import com.comduck.chatbot.discord.audiocore.webutil.YoutubeWebUtil;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchProvider;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.FileUpload;
import org.apache.hc.core5.http.ParseException;
import org.jetbrains.annotations.Nullable;
import org.json.simple.JSONObject;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * @Desc 각 서버에 할당되는 오디오 인스턴스
 */
public class PlayerInstance {

    public final AudioPlayerManager playerManager;
    public final TrackScheduler trackScheduler;

    public final AudioPlayer player;

    public Message lastPlayMessage = null;

    public PlayerInstance()
    {
        playerManager = new DefaultAudioPlayerManager();
        playerManager.registerSourceManager(new YoutubeAudioSourceManager());
        playerManager.registerSourceManager(new BandcampAudioSourceManager());
        playerManager.registerSourceManager(SoundCloudAudioSourceManager.createDefault());
        playerManager.registerSourceManager(new HttpAudioSourceManager());
        playerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
        playerManager.registerSourceManager(SoundCloudAudioSourceManager.createDefault());

        try { // 트위치 연결 오류 때문에 로그 도배 되는거 방지
            AudioSourceManagers.registerRemoteSources(playerManager);
        } catch (Exception e) {}
        player = playerManager.createPlayer();

        trackScheduler = new TrackScheduler(player);
        player.addListener(trackScheduler);
    }

    /**
     * 트랙 재생 및 채널 접속
     *
     * @param textChannel 재생 메시지를 출력할 텍스트 채널
     * @param voiceChannel 음악을 재생할 음성 채널
     * @param video 재생할 비디오 Uri
     */
    public void PlayTrackTo(GenericEvent event, final TextChannel textChannel, final VoiceChannel voiceChannel, String video, @Nullable Consumer<AudioTrack> callback) throws IOException, ParseException, SpotifyWebApiException {
        try {
            var audioManager = textChannel.getGuild().getAudioManager();
            if(!audioManager.isConnected()) audioManager.openAudioConnection(voiceChannel);
            audioManager.setSendingHandler(new AudioPlayerSendHandler(player));
        }
        catch (Exception e)
        {
            if(callback != null) callback.accept(null);
            textChannel.sendMessage("채널 접속에 실패했습니다.").queue();
        }
        Platform TrackType = Platform.http;

        if(video.contains("spotify"))
        {
            TrackType = Platform.Spotify;
            YoutubeSearchProvider musicProvider = new YoutubeSearchProvider();

            Track track = SpotifyWebUtil.getTrack(textChannel.getGuild(), video);

            String searchTitle = String.format("[Topic] %s - %s", track.getName(), track.getArtists()[0].getName());

            System.out.println(searchTitle);

            List<AudioTrackInfo> tracks = new ArrayList<AudioTrackInfo>();
            musicProvider.loadSearchResult(searchTitle, x -> {
                tracks.add(x);
                return null;
            });

            video = tracks.get(0).uri;

            System.out.println(String.format("[PlayInstance#%s] Spotify Track convert to Youtube Track : %s", textChannel.getGuild().getId(), tracks.get(0).uri));
        }
        if(video.contains("youtu"))
            TrackType = Platform.Youtube;
        else if(video.contains("soundcloud"))
            TrackType = Platform.Soundcloud;
        else
            TrackType = Platform.http;

        Platform finalTrackType = TrackType;
        playerManager.loadItem(video, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                final AudioTrackInfo trackInfo = track.getInfo();
                final File playingImage;
                String[] tags = {};

                switch (finalTrackType)
                {
                    case Other:
                    case Youtube:
                        tags = YoutubeWebUtil.getTrackTags(trackInfo.identifier);
                    default:
                        YoutubeParse yp = new YoutubeParse();
                        playingImage = new YoutubePlayingImageProcessor().processImage(event, track, tags, trackInfo.author, trackInfo.title, yp.getVideo(track.getIdentifier()).get("author_url").toString());
                        break;

                    case http:
                        if(callback != null) callback.accept(track);
                        textChannel.sendMessage("> Play audio file\n" + track.getInfo().title + " | " + track.getInfo().author).queue(send_msg -> {
                            trackScheduler.queue(new TrackMessage(send_msg, event, track));
                        });
                        return;

                    case Soundcloud:
                        if(callback != null) callback.accept(track);
                        textChannel.sendMessage("> Play SoundCloud\n" + track.getInfo().title + " | " + track.getInfo().author).queue(send_msg -> {
                            trackScheduler.queue(new TrackMessage(send_msg, event, track));
                        });
                        return;

                    case Spotify:
                        tags = new String[]{"Spotify"};
                        playingImage = new YoutubePlayingImageProcessor().processImage(event, track, tags, trackInfo.author, trackInfo.title, "");
                        break;
                }

                if(callback != null) callback.accept(track);

                var msg = textChannel.sendFiles(FileUpload.fromData(playingImage));
                msg.queue(send_msg -> {
                    trackScheduler.queue(new TrackMessage(send_msg, event, track));
                });
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AtomicReference<Message> send_msg = new AtomicReference<>();
                textChannel.sendMessage(String.format("플레이리스트를 추가합니다. ``%s`` [%d]", playlist.getName(), playlist.getTracks().size())).queue(msg -> {
                    send_msg.set(msg);
                });

                for (AudioTrack track : playlist.getTracks()) {
                    trackScheduler.queue(new TrackMessage(send_msg.get(), event, track));
                }

                if(callback != null) callback.accept(null);
            }

            @Override
            public void noMatches() {
                if(callback != null) callback.accept(null);
                textChannel.sendMessage("곡이 존재하지 않습니다.").queue();
            }

            @Override
            public void loadFailed(FriendlyException e) {
                if(callback != null) callback.accept(null);
                textChannel.sendMessage("곡을 불러올 수 없습니다.").queue();
                e.printStackTrace();
            }
        });
    }
}
