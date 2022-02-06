package com.comduck.chatbot.discord.audiocore;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.model.YoutubeVideo;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.RestAction;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.json.simple.JSONObject;
import com.comduck.chatbot.discord.imgproc.ImageProcessor;
import com.comduck.chatbot.discord.imgproc.ImgprocTwo;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private URL searchIcon(String youtubechennal) {
        //유튜브 검색 경로 지정, 빈 해쉬맵 생성
        Document doc2 = null;
        //1. URL + title 로 검색하여 영상 제목을 전부 가져옴
        //2. video 해쉬맵에 하나씩 담아서 반환
        try {
            doc2 = Jsoup.connect(youtubechennal).get();

            Element imageE = doc2.body().select("link[rel^=image_]").first();
            return new URL(imageE.attr("href"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String[] GetVideoTags(String videoIdentifier) {

        List<String> tags = new ArrayList<String>();
        try {
            YoutubeDownloader yd = new YoutubeDownloader();
            YoutubeVideo video = yd.getVideo(videoIdentifier);
            String dsc = video.details().description();

            Pattern p = Pattern.compile("(#(\\S)*)");
            Matcher m = p.matcher(dsc);

            while (m.find()) {
                tags.add(m.group());
            }
        }catch (Exception e) {
            System.out.println("[PlayerManager] Failed Load Tag list");
        }
        return tags.toArray(new String[tags.size()]);
    }

    private void loadAndPlay_Msg(MessageReceivedEvent event, String trackUrl) {

        final GuildMusicManager musicManager = getGuildMusicManager(event.getGuild());

        //트랙 로드, 에러 관련 처리/이벤트
        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            public void trackLoaded(AudioTrack track) {
                //RestAction<Message> loadingMsgAction = event.getChannel().sendMessage("> " + track.getInfo().title + " 불러오는중...");
                //Message loadingMsg = loadingMsgAction.complete();


                boolean isYoutube = trackUrl.contains("youtu");
                AudioTrackInfo trackInfo = track.getInfo();
                if(isYoutube) {
                    File img = null;
                    File canvasFileFull = null;
                    File canvasFileReduction = null;
                    URL requesterIconFile = null;
                    URL uploaderIconFile = null;
                    JSONObject trackVideo = null;
                    String[] tags;

                    ImageProcessor imgProcessor = new ImageProcessor();
                    ImgprocTwo imgprocTwo = new ImgprocTwo();

                    YoutubeParse yp = new YoutubeParse();

                    try {
                        String path = System.getProperty("user.dir");
                        System.out.println("Working Directory = " + path);

                        trackVideo = yp.getVideo(track.getIdentifier());
                        canvasFileFull = new File("PlayerTempletF2.png");
                        canvasFileReduction = new File("PlayerTempletR.png");
                        requesterIconFile = new URL(event.getAuthor().getAvatarUrl());
                        uploaderIconFile = searchIcon(trackVideo.get("author_url").toString());

                        tags = GetVideoTags(trackInfo.identifier);
                        for (int i = 0; i< tags.length ; i++){

                            System.out.println(tags[i]);
                        }

                        String query = "SELECT * FROM ServerSetting WHERE id=%s";
                        Connection connection = DriverManager.getConnection("jdbc:sqlite:log.db");
                        PreparedStatement preparedStatement = connection.prepareStatement(String.format(query, event.getGuild().getId()));
                        int playingDisplay = preparedStatement.executeQuery().getInt("PlayDisplay");

                        MessageAction action = null;

                        preparedStatement.close();
                        //loadingMsg.editMessage("> 이미지 생성중...").queue();
                        if (playingDisplay == 0) {
                            img = imgProcessor.processImage(trackVideo, canvasFileFull, requesterIconFile, uploaderIconFile, event.getAuthor().getName(), track.getDuration(), tags);
                            //loadingMsg.delete().queue();
                            action = event.getChannel().sendFile(img);
                        } else if (playingDisplay == 1) {
                            img = imgprocTwo.processImage(trackVideo, canvasFileReduction, requesterIconFile, uploaderIconFile, event.getAuthor().getName());
                            //loadingMsg.delete().queue();
                            action = event.getChannel().sendFile(img);
                        } else {
                            EmbedBuilder eb = new EmbedBuilder();
                            eb.setColor(new Color(0x244aff));
                            eb.addField(trackInfo.title, String.format("곡이 대기열에 추가되었습니다.\n``%s``", event.getAuthor().getName()), false);
                            action = event.getChannel().sendMessage(eb.build());
                        }

                        action.queue();
                        event.getMessage().delete().queue();

                    } catch (Exception e) {
                        e.printStackTrace();
                        //event.getChannel().sendFile(new File("AuthERROR.png")).queue();
                    }
                } else {
                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setColor(new Color(0x244aff));
                    eb.addField(trackInfo.title, String.format("곡이 대기열에 추가되었습니다.\n``%s``", event.getAuthor().getName()), false);
                    //loadingMsg.delete().queue();
                    event.getChannel().sendMessage(eb.build()).queue();
                }
                play(musicManager, track);
                YoutubeAudioSourceManager pm = new YoutubeAudioSourceManager();
                playerManager.registerSourceManager(pm);

            }

            public void playlistLoaded(AudioPlaylist audioPlaylist) {

                Object[] tracks = audioPlaylist.getTracks().toArray();
                System.out.println(tracks);
                String listTitle = audioPlaylist.getName();

                EmbedBuilder eb = new EmbedBuilder();
                eb.setColor(new Color(0xE741FF));
                eb.addField(
                        String.format("리스트가 대기열에 추가되었습니다."),
                        String.format(
                                "%s\n``[ +%d곡 ] - %s``",
                                listTitle,
                                tracks.length,
                                event.getAuthor().getName()
                        ), false);
                event.getChannel().sendMessage(eb.build()).queue();
                event.getMessage().delete().queue();
                for (Object track : tracks) {
                    play(musicManager, (AudioTrack) track);
                }

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
