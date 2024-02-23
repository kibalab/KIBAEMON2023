package com.comduck.chatbot.discord.audiocore;

import com.comduck.chatbot.discord.audiocore.enums.Platform;
import com.comduck.chatbot.discord.audiocore.imgproc.SpotifyPlayingImageProcessor;
import com.comduck.chatbot.discord.audiocore.webutil.SpotifyWebUtil;
import com.comduck.chatbot.discord.audiocore.webutil.YoutubeWebUtil;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.beam.BeamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchProvider;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;
import net.dv8tion.jda.api.utils.FileUpload;
import org.apache.hc.core5.http.ParseException;
import org.json.simple.JSONObject;
import com.comduck.chatbot.discord.audiocore.imgproc.YoutubePlayingImageProcessor;
import com.comduck.chatbot.discord.audiocore.imgproc.ImgprocTwo;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class PlayerManager {
    private static PlayerManager INSTANCE;
    public final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;

    private PlayerManager() {
        this.musicManagers = new HashMap<Long, GuildMusicManager>();

        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);

        YoutubeAudioSourceManager pm = new YoutubeAudioSourceManager();
        this.playerManager.registerSourceManager(pm);
        this.playerManager.registerSourceManager(new BandcampAudioSourceManager());
        this.playerManager.registerSourceManager(new VimeoAudioSourceManager());
        this.playerManager.registerSourceManager(new BeamAudioSourceManager());
        this.playerManager.registerSourceManager(new HttpAudioSourceManager());
        this.playerManager.registerSourceManager(SoundCloudAudioSourceManager.builder().build());
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

    private void loadAndPlay_Msg(MessageReceivedEvent event, String trackUrl, Platform platform) throws IOException, ParseException, SpotifyWebApiException {

        final GuildMusicManager musicManager = getGuildMusicManager(event.getGuild());

        AtomicReference<String> formattedTrackUrl = new AtomicReference<>("");

        switch (platform)
        {
            case Spotify:
                YoutubeSearchProvider musicProvider = new YoutubeSearchProvider();

                Track track = SpotifyWebUtil.getTrack(event.getGuild(), trackUrl);

                String searchTitle = String.format("[Topic] %s - %s", track.getName(), track.getArtists()[0].getName());

                System.out.println(searchTitle);

                List<AudioTrackInfo> tracks = new ArrayList<AudioTrackInfo>();
                musicProvider.loadSearchResult(searchTitle, x -> {
                    tracks.add(x);
                    return null;
                });
                formattedTrackUrl.set(tracks.get(0).uri);
                System.out.println(tracks.get(0).title);
                break;
            default:
                formattedTrackUrl.set(trackUrl);
        }


        //트랙 로드, 에러 관련 처리/이벤트x
        playerManager.loadItemOrdered(musicManager, formattedTrackUrl.get(), new AudioLoadResultHandler() {
            public void trackLoaded(AudioTrack track) {
                AudioTrackInfo trackInfo = track.getInfo();

                switch (platform)
                {
                    case Youtube:
                        try {
                            MessageCreateAction action = BuildPlayingMessage(track, trackInfo);

                            //Switch Message
                            action.queue();
                            event.getMessage().delete().queue();

                        } catch (Exception e) {
                            e.printStackTrace();
                            event.getChannel().sendFiles(FileUpload.fromData(new File("AuthERROR.png"))).queue();
                        }
                        break;
                    case Spotify:
                        try {
                            MessageCreateAction action = BuildPlayingMessage(track, trackInfo);

                            //Switch Message
                            action.queue();
                            event.getMessage().delete().queue();

                        } catch (Exception e) {
                            e.printStackTrace();
                            event.getChannel().sendFiles(FileUpload.fromData(new File("AuthERROR.png"))).queue();
                        }
                        break;
                    default:
                        EmbedBuilder eb = new EmbedBuilder();
                        eb.setColor(new Color(0x244aff));
                        eb.addField(trackInfo.title, String.format("곡이 대기열에 추가되었습니다.\n``%s``", event.getAuthor().getName()), false);
                        //loadingMsg.delete().queue();
                        event.getChannel().sendMessageEmbeds(eb.build()).queue();
                        break;

                }


                play(musicManager, track);
            }

            public MessageCreateAction BuildPlayingMessage(AudioTrack track, AudioTrackInfo trackInfo) throws IOException, ParseException, SpotifyWebApiException {
                File img = null;
                File canvasFileFull = null;
                File canvasFileReduction = null;
                URL requesterIconFile = null;
                URL uploaderIconFile = null;
                JSONObject trackVideo = null;
                String[] tags = null;
                MessageCreateAction action = null;

                ImgprocTwo imgprocTwo = new ImgprocTwo();

                YoutubeParse yp = new YoutubeParse();

                tags = YoutubeWebUtil.getTrackTags(trackInfo.identifier);

                String path = System.getProperty("user.dir");
                System.out.println("[PlayerManager] Working Directory : " + path);

                trackVideo = yp.getVideo(track.getIdentifier());
                canvasFileFull = new File("PlayerTempletF3.png");
                canvasFileReduction = new File("PlayerTempletR.png");
                requesterIconFile = new URL(event.getAuthor().getAvatarUrl());
                uploaderIconFile = YoutubeWebUtil.getUserImage(trackVideo.get("author_url").toString());

                switch (platform)
                {
                    case Youtube:
                        YoutubePlayingImageProcessor youtubePlayingImageProcessor = new YoutubePlayingImageProcessor();
                        img = youtubePlayingImageProcessor.processImage(trackVideo, event.getAuthor(),uploaderIconFile, track.getDuration(), tags);
                        //loadingMsg.delete().queue();
                        action = event.getChannel().sendFiles(FileUpload.fromData(img));
                        break;
                    case Spotify:
                        SpotifyPlayingImageProcessor spotifyPlayingImageProcessor = new SpotifyPlayingImageProcessor();
                        img = spotifyPlayingImageProcessor.processImage(event.getGuild(), SpotifyWebUtil.getTrack(event.getGuild(), trackUrl), event.getAuthor(), track.getDuration(), tags);
                        //loadingMsg.delete().queue();
                        action = event.getChannel().sendFiles(FileUpload.fromData(img));
                        break;
                    default:
                        EmbedBuilder eb = new EmbedBuilder();
                        eb.setColor(new Color(0x244aff));
                        eb.addField(trackInfo.title, String.format("곡이 대기열에 추가되었습니다.\n``%s``", event.getAuthor().getName()), false);
                        action = event.getChannel().sendMessageEmbeds(eb.build());
                }

                action.addActionRow(
                        Button.primary("pause", "Pause").withStyle(ButtonStyle.SECONDARY),
                        Button.primary("stop", "Stop").withStyle(ButtonStyle.SECONDARY),
                        Button.primary("skip", "Skip").withStyle(ButtonStyle.SECONDARY),
                        Button.primary("track", "Track List").withStyle(ButtonStyle.SECONDARY)
                );

                return action;
            }

            public void playlistLoaded(AudioPlaylist audioPlaylist) {

                Object[] tracks = audioPlaylist.getTracks().toArray();
                System.out.println(tracks);
                String listTitle = audioPlaylist.getName();

                EmbedBuilder eb = new EmbedBuilder();
                eb.setColor(new Color(0xE741FF));
                eb.addField(
                        "리스트가 대기열에 추가되었습니다.",
                        String.format(
                                "%s\n``[ +%d곡 ] - %s``",
                                listTitle,
                                tracks.length,
                                event.getAuthor().getName()
                        ), false);
                event.getChannel().sendMessageEmbeds(eb.build()).queue();
                event.getMessage().delete().queue();
                for (Object track : tracks) {
                    play(musicManager, (AudioTrack) track);
                }

            }

            public void noMatches() {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setColor(new Color(0xff3e3e));
                eb.addField(trackUrl, String.format("곡을 찾을수 없습니다.\n ``No Matches`` ``%s``", event.getAuthor().getName()), false);
                event.getChannel().sendMessageEmbeds(eb.build()).queue();
            }

            public void loadFailed(FriendlyException e) {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setColor(new Color(0xff3e3e));
                eb.addField(trackUrl, String.format("곡을 찾을수 없습니다.\n ``LoadFailed`` ``%s``", event.getAuthor().getName()), false);
                event.getChannel().sendMessageEmbeds(eb.build()).queue();
                event.getChannel().sendMessage("> " + e.toString()).queue();
            }
        });
    }

    private void loadAndPlay_Reaction(GenericMessageReactionEvent event, String trackUrl) {

        final GuildMusicManager musicManager = getGuildMusicManager(event.getGuild());

        //트랙 로드, 에러 관련 처리/이벤트
        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            public void trackLoaded(AudioTrack track) {
                /*EmbedBuilder eb = new EmbedBuilder();
                eb.setColor(new Color(0x244aff));
                eb.addField(track.getInfo().title, String.format("곡이 대기열에 다시 추가되었습니다.\n``%s``", event.getUser().getName()), false);*/
                event.getChannel().sendMessage(String.format("> 대기열에 곡 재신청 ``%s``", event.getUser().getName())).queue();

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
                event.getChannel().sendMessageEmbeds(eb.build()).queue();

                play(musicManager, firstTrack);
            }

            public void noMatches() {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setColor(new Color(0xff3e3e));
                eb.addField(trackUrl, String.format("곡을 찾을수 없습니다.\n``%s``", event.getUser().getName()), false);
                event.getChannel().sendMessageEmbeds(eb.build()).queue();
            }

            public void loadFailed(FriendlyException e) {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setColor(new Color(0xff3e3e));
                eb.addField(trackUrl, String.format("곡을 찾을수 없습니다.\n``%s``", event.getUser().getName()), false);
                event.getChannel().sendMessageEmbeds(eb.build()).queue();
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
        Platform platform = Platform.Other;
        
        if(trackUrl.contains("youtu"))
            platform = Platform.Youtube;
        else if (trackUrl.contains("spotify"))
            platform = Platform.Spotify;
        else if (trackUrl.contains("soundcloud"))
            platform = Platform.Soundcloud;
        else
            platform = Platform.Other;

        if (event instanceof MessageReceivedEvent) {
            try {
                loadAndPlay_Msg((MessageReceivedEvent) event, trackUrl, platform);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            } catch (SpotifyWebApiException e) {
                throw new RuntimeException(e);
            }
        } else if (event instanceof GenericMessageReactionEvent) {
            loadAndPlay_Reaction((GenericMessageReactionEvent) event, trackUrl); // 삭제 예정
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
