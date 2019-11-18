package com.comduck.chatbot.discord.audiocore;

import com.comduck.chatbot.discord.imgproc.ImageProcessor;
import com.comduck.chatbot.discord.imgproc.ImgprocTwo;
import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.YoutubeException;
import com.github.kiulian.downloader.model.VideoDetails;
import com.github.kiulian.downloader.model.YoutubeVideo;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
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

    private URL searchIcon(String videoId, String uploaderName) {
        //유튜브 검색 경로 지정, 빈 해쉬맵 생성
        String youtubeUrl = "https://www.youtube.com/watch?v=";
        String youtubechennal = "";
        Document doc = null, doc2 = null;
        //1. URL + title 로 검색하여 영상 제목을 전부 가져옴
        //2. video 해쉬맵에 하나씩 담아서 반환
        try {
            doc = Jsoup.connect(youtubeUrl+videoId).get();
            Elements titleE = doc.getElementsByTag("a");
            for(int i=0; titleE.size()>i ; i++) {
                Element data = titleE.get(i);
                //System.out.println("\n[" + i + "]TestParse: " + data.attr("id") + "\n" + data.className() + "\n" +data.attr("href"));
                //System.out.println(i + " " + data.text());
                if(data.attr("href").contains("/channel/")) {
                    youtubechennal += "https://www.youtube.com" + data.attr("href");
                    doc2 = Jsoup.connect(youtubechennal).get();
                    break;
                }
                /*
                if(uploaderName.endsWith("VEVO")) {

                    try {
                        youtubechennal = "https://www.youtube.com/user/" + uploaderName;
                        doc2 = Jsoup.connect(youtubechennal).get();
                    } catch (Exception e) {
                        youtubechennal = "https://www.youtube.com" + data.attr("href");
                        doc2 = Jsoup.connect(youtubechennal).get();
                    }

                    break;
                }
                if( data.text().equals(uploaderName)) {
                    youtubechennal = "https://www.youtube.com" + data.attr("href");
                    doc2 = Jsoup.connect(youtubechennal).get();
                break;
                }*/
            }
            Elements imageE = doc2.getElementsByTag("img");
            for(int i=0; imageE.size()>i ; i++) {
                Element data = imageE.get(i);
                if( data.attr("src").startsWith("https://") ) {
                    //System.out.println("\n[" + i + "]TestParse: " + data.text() + "\n" + data.className() + "\n" +data.attr("src"));
                    return new URL(data.attr("src"));
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return null;
}

    private void loadAndPlay_Msg(MessageReceivedEvent event, String trackUrl) {


        final GuildMusicManager musicManager = getGuildMusicManager(event.getGuild());

        //트랙 로드, 에러 관련 처리/이벤트
        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            public void trackLoaded(AudioTrack track) {
                boolean isYoutube = trackUrl.contains("youtu");
                if(isYoutube) {
                    File canvasFileFull = null;
                    File canvasFileReduction = null;
                    URL requesterIconFile = null;
                    URL uploaderIconFile = null;
                    String videoId = track.getInfo().identifier;
                    try {
                        canvasFileFull = new File("PlayerTempletF.png");
                        //canvasFileReduction = new File("PlayerTempletR.png");

                        String id = (track.getInfo().uri).replace("https://", "");
                        id = id.replace("watch?v=", "").split("/")[1];

                        requesterIconFile = new URL(event.getAuthor().getAvatarUrl());
                        uploaderIconFile = searchIcon(track.getInfo().identifier, track.getInfo().author);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }


                    YoutubeVideo trackVideo = null;
                    try {
                        trackVideo = YoutubeDownloader.getVideo(track.getIdentifier());
                    } catch (YoutubeException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    ImageProcessor imgProcessor = new ImageProcessor();
                    ImgprocTwo imgprocTwo = new ImgprocTwo();
                    File img = null;
                    try {
                        img = imgProcessor.processImage(trackVideo.details(), canvasFileFull, requesterIconFile, uploaderIconFile, event.getAuthor().getName());
                        //img = imgprocTwo.processImage(trackVideo.details(), canvasFileReduction, requesterIconFile, uploaderIconFile, event.getAuthor().getName());

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    event.getChannel().sendFile(img).queue();
                    event.getMessage().delete().queue();
                } else {
                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setColor(new Color(0x244aff));
                    eb.addField(track.getInfo().title, String.format("곡이 대기열에 추가되었습니다.\n``%s``", event.getAuthor().getName()), false);
                    event.getChannel().sendMessage(eb.build()).queue();
                }

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
