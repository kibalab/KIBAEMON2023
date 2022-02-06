package com.comduck.chatbot.discord;

import com.comduck.chatbot.discord.DiscordBotMain;
import com.comduck.chatbot.discord.audiocore.GuildMusicManager;
import com.comduck.chatbot.discord.audiocore.PlayerManager;
import com.comduck.chatbot.discord.audiocore.PostCommandListener;
import com.comduck.chatbot.discord.audiocore.TrackScheduler;
import com.comduck.chatbot.discord.minigame.Roulette;
import com.comduck.chatbot.discord.naverapi.Papago;
import com.comduck.chatbot.discord.naverapi.Shopping;
import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.YoutubeException;
import com.github.kiulian.downloader.model.YoutubeVideo;
import com.sedmelluq.discord.lavaplayer.filter.AudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.PcmFilterFactory;
import com.sedmelluq.discord.lavaplayer.filter.UniversalPcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.format.AudioDataFormat;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.TrackMarker;
import com.sedmelluq.discord.lavaplayer.track.TrackMarkerHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.internal.utils.JDALogger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ShortBuffer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandManager {

    Roulette roulette = new Roulette();

    public GuildMusicManager musicManager; // = manager.getGuildMusicManager(event.getGuild());
    AudioPlayer player; // = musicManager.player;
    TrackScheduler scheduler; // = musicManager.scheduler;

    List<PostCommandListener> postCommandListeners;

    int globalVolume = 10;

    public CommandManager(GuildMusicManager guildMusicManager, AudioPlayer player, TrackScheduler scheduler) {
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

    public void helpCommand(GenericMessageEvent event) {
        String msg = "***임시 명령어 도움말***\r"+
"```접두사 : '?' (명령어를 사용할때 가장앞에 반드시 포함되어있어야 합니다)\r"+
"help : 명령어 도움말 입니다. [hlp로 대체가능]\r\r"+
"Music\r"+
"    play [Url또는Title] : 음악을 재생합니다.\r"+
"    - PlayingDisplay : 재생 메시지의 디자인을 선택합니다.\r"+
"    pause : 음악을 일시정지 합니다\r"+
"    join [음성채널ID] : 음성채널에 봇을 추가합니다.(ID가 없으면 요청자가 있는 방에 추가됩니다)\r"+
"    leave : 음성채널에서 내보냅니다. [out으로 대체가능]\r"+
"    stop : 대기열을 초기화 합니다.\r"+
"    skip : 다음곡으로 넘어갑니다. [next로 대체가능]\r"+
"    volume [0과 100사이값] : 음량을 조절합니다. [vol로 대체가능]\r"+
"    tracklist : 대기열을 출력합니다. [songlist, tlist, slist로 대체가능]\r"+
"    goto [시간 00:00] : 현재곡의 재생을 지정한 시간으로 이동합니다.\r"+
"    shuffle : 대기열을 무작위로 섞습니다. [mix, sf로 대체가능]\r"+
"    repeat : 현재 재생중인 곡을 다시 대기열에 추가합니다. [replay, rp로 대체가능]\r"+
"    clear : 텍스트채널의 문자 50개씩 삭제합니다.\r"+
"Utility\r"+
"    papago [언어] [텍스트] : 파파고 엔진으로 번역합니다.\r"+
"    shopping [제품이름] : 네이버 쇼핑에서 상품을 검색합니다. [shop로 대체가능]\r"+
"    roulette [베팅금액] : 룰렛을 돌립니다.(시작 500만원/최소 천원/최대 만원) [rol로 대체가능]\r"+
"    hangang : 한강온도를 표시합니다.\r"+
"    samsung : 현재 삼성전자 주식을 확인합니다. (한강온도표시도 포함)\r"+
"```\r"+
"제작 : KIBA#4466\r";
        event.getChannel().sendFile(new File("play_help.png")).queue();
        event.getChannel().sendMessage(msg).queue();
    }

    public void playCommand(GenericMessageEvent event, String msg) {
        VoiceChannel Vch = null;
        PlayerManager manager = PlayerManager.getInstance();
        AudioManager audiomng = event.getGuild().getAudioManager();
        String url = msg.replaceFirst("play", "").replace(" ", "");

        // GenericMessageEvent 종속 메소드 분리
        if (event instanceof MessageReceivedEvent) {
            MessageReceivedEvent msgEvent = (MessageReceivedEvent) event;
            Vch = ((MessageReceivedEvent) event).getMember().getVoiceState().getChannel();
        } else {
            GenericMessageReactionEvent reactionEvent = (GenericMessageReactionEvent) event;
            Vch = ((GenericMessageReactionEvent) event).getMember().getVoiceState().getChannel();
        }

        if (!audiomng.isConnected()) {
            audiomng.openAudioConnection(Vch);
        }


        //1. 파라미터 없으면 현재 재생되고 있는 음액 재신청
        //2. URL이 아니면 유튜브에 검색
        if(url.equals("")) {
            url = this.player.getPlayingTrack().getInfo().uri;
        } else if (url.startsWith("<#")){

            TextChannel ch = event.getJDA().getTextChannelById(url.replace("<#", "").replace(">", ""));
            List<Message> msgs = ch.getIterableHistory().complete();
            Collections.shuffle(msgs);
            for(Message m : msgs) {
                if (isURL(m.getContentDisplay()) && m.getContentDisplay().contains("youtu")) {
                    //음악 재생
                    manager.loadAndPlay(event, m.getContentDisplay());
                    manager.getGuildMusicManager(event.getGuild()).player.setVolume(globalVolume);
                }
            }
            return;
        }else {
            System.out.println(url);
            if (isURL(url)) {
                //음악 재생
                manager.loadAndPlay(event, url);
                manager.getGuildMusicManager(event.getGuild()).player.setVolume(globalVolume);
            } else {
                ArrayList<String> video = searchYoutube(url);
                url = video.get(0);
                //음악 재생
                manager.loadAndPlay(event, url);
                manager.getGuildMusicManager(event.getGuild()).player.setVolume(globalVolume);
            }
        }
        //커맨드 실행 기록 이벤트 발생
        raisePostCommand(event);
    }

    public void pauseCommand(GenericMessageEvent event) {
        if (!this.player.isPaused()) {
            this.player.setPaused(true);
        } else {
            this.player.setPaused(false);
        }


        if (event instanceof MessageReceivedEvent) {
            MessageReceivedEvent msgEvent = (MessageReceivedEvent) event;
            event.getChannel().sendMessage(String.format("> 일시정지 ``%s``", ((MessageReceivedEvent) event).getAuthor().getName()));
        } else if (event instanceof GenericMessageReactionEvent) {
            GenericMessageReactionEvent reactionEvent = (GenericMessageReactionEvent) event;
            event.getChannel().sendMessage(String.format("> 일시정지 ``%s``", ((GenericMessageReactionEvent) event).getUser().getName()));
        }
    }

    public void joinCommand(GenericMessageEvent event, String msg) {
        String VchID = msg.replaceFirst("join", "").replace(" ", "");
        VoiceChannel Vch = null;

        if (event instanceof MessageReceivedEvent) {

            //1. 파라미터값이 있으면 해당 VoiceChennal을 가져옴
            //2. 없으면 요청한 유저가 있는 VoiceChennal을 가져옴
            if(!VchID.equals("")) {
                Vch = ((MessageReceivedEvent) event).getGuild().getVoiceChannelById(VchID);
            } else {
                Vch = ((MessageReceivedEvent) event).getMember().getVoiceState().getChannel();
            }

            event.getChannel().sendMessage(String.format(
                    "> %s 입장 ``%s``",
                    Vch.getName(),
                    ((MessageReceivedEvent) event).getAuthor().getName()
            )).queue();

        }
        AudioManager audiomng = event.getGuild().getAudioManager();

        // VoiceChennal에 입장, 안되면 오류Embed 출력
        try {
            audiomng.openAudioConnection(Vch);
        } catch (Exception e) {

            EmbedBuilder eb = new EmbedBuilder();
            eb.setColor(new Color(0xFF1A1E));
            eb.addField("오류 Error", String.format(
                    "%s 에 입장할수 없습니다.\n``%s``",
                    Vch.getName(),
                    ((MessageReceivedEvent) event).getAuthor().getName().toString()
            ), false);
            event.getChannel().sendMessage(eb.build()).queue();

        }

    }

    public void leaveCommand(GenericMessageEvent event) {
        VoiceChannel Vch = null;

        if (event instanceof MessageReceivedEvent) {
            Vch = ((MessageReceivedEvent) event).getMember().getVoiceState().getChannel();
            event.getChannel().sendMessage(String.format(
                    "> %s 퇴장 ``%s``",
                    Vch.getName(),
                    ((MessageReceivedEvent) event).getAuthor().getName()
            )).queue();
        }
        event.getGuild().getAudioManager().closeAudioConnection();

        //stopCommand와 같은 부분
        this.scheduler.getQueue().clear();
        this.player.stopTrack();
        this.player.setPaused(false);
    }

    public void stopCommand(GenericMessageEvent event) {

        //재생되고 있는 트랙이 있는지 확인
        if (this.player.getPlayingTrack() == null) {
            return;
        }

        //GenericMessageEvent 종속 메소드 분리후 처리
        if (event instanceof MessageReceivedEvent) {
            MessageReceivedEvent msgEvent = (MessageReceivedEvent)event;

            msgEvent.getChannel().sendMessage(String.format(
                    "> 대기열 재생 중지 ``%s``",
                    msgEvent.getAuthor().getName()
            )).queue();
        } else if (event instanceof GenericMessageReactionEvent) {
            GenericMessageReactionEvent reactionEvent = (GenericMessageReactionEvent)event;

            reactionEvent.getChannel().sendMessage(String.format(
                    "> 대기열 재생 중지 ``%s``",
                    reactionEvent.getUser().getName()
            )).queue();
        }

        //1. tracklist를 초기화
        //2. 현재 재생되고 있는 트랙 정지
        //3. 플레이어 일시정지
        this.scheduler.getQueue().clear();
        this.player.stopTrack();
        this.player.setPaused(false);

        //커맨드 실행 기록 이벤트 발생
        raisePostCommand(event);
    }

    public void skipCommand(GenericMessageEvent event) {

        //재생되고 있는 트랙이 있는지 확인
        if (this.player.getPlayingTrack() == null) {
            return;
        }

        // Stop과 같은 처리구조
        if (event instanceof MessageReceivedEvent) {
            MessageReceivedEvent msgEvent = (MessageReceivedEvent) event;
            if (this.player.getPlayingTrack() == null) {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setColor(new Color(0xff6624));
                eb.addField("경고 Warning", String.format(
                        "대기열이 비어 있습니다.\n``%s``",
                        ((MessageReceivedEvent) event).getAuthor().getName().toString()
                ), false);
                event.getChannel().sendMessage(eb.build()).queue();
            } else {
                event.getChannel().sendMessage(String.format(
                        "> 곡 스킵 ``%s``",
                        ((MessageReceivedEvent) event).getAuthor().getName()
                )).queue();
                this.scheduler.nextTrack();
            }
        } else if (event instanceof GenericMessageReactionEvent) {
            GenericMessageReactionEvent reactionEvent = (GenericMessageReactionEvent) event;
            if (this.player.getPlayingTrack() == null) {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setColor(new Color(0xff6624));
                eb.addField("경고 Warning", String.format(
                        "대기열이 비어 있습니다.\n``%s``",
                        ((GenericMessageReactionEvent) event).getUser().getName().toString()
                ), false);
                event.getChannel().sendMessage(eb.build()).queue();
            } else {
                event.getChannel().sendMessage(String.format(
                        "> 곡 스킵 ``%s``",
                        ((GenericMessageReactionEvent) event).getUser().getName()
                )).queue();
                this.scheduler.nextTrack();
            }
        }
    }

    public void volumeCommand(GenericMessageEvent event, String msg) {

        //재생되고 있는 트랙이 있는지 확인
        if (this.player.getPlayingTrack() == null) {
            return;
        }

        //파라미터 분리
        String _Nvol = msg.replaceFirst("volume ", "");

        //현재 음량과 요청 음량을 가져옴
        int Ovol = this.player.getVolume();
        int Nvol = Integer.parseInt(_Nvol);

        //최대 음량 제한
        if (Nvol > 100) {
            Nvol = 100;
        }

        event.getChannel().sendMessage(String.format(
                "> 음량 제어 %d->%s",
                Ovol,
                Nvol
        )).queue();

        //1.다음 곡도 같은 음량을 유지하기 위해 전역변수에 담음
        //2.플레이어 음량 변경
        globalVolume = Nvol;
        this.player.setVolume(globalVolume);

    }

    public void tracklistCommand(GenericMessageEvent event) {

        //재생되고 있는 트랙이 있는지 확인
        // (재생되고 있는 트랙이 없으면 tracklist도 비어있는걸로 판단)
        if (player.getPlayingTrack() == null) {
            EmbedBuilder eb = new EmbedBuilder();
            eb.setColor(new Color(0xff6624));
            if (event instanceof MessageReceivedEvent) {
                MessageReceivedEvent msgEvent = (MessageReceivedEvent) event;
                eb.addField("경고 Warning", String.format(
                        "대기열이 비어 있습니다.\n``%s``",
                        ((MessageReceivedEvent) event).getAuthor().getName().toString()
                ), false);
            } else if (event instanceof GenericMessageReactionEvent) {
                GenericMessageReactionEvent reactionEvent = (GenericMessageReactionEvent) event;
                eb.addField("경고 Warning", String.format(
                        "대기열이 비어 있습니다.\n``%s``",
                        ((GenericMessageReactionEvent) event).getUser().getName().toString()
                ), false);
            }
            event.getChannel().sendMessage(eb.build()).queue();
        } else { // 비어있지 않으면

            //현재재생되고 있는 트랙의 정보를 가져옴 (title, Position, Duration)
            AudioTrackInfo info = player.getPlayingTrack().getInfo();

            EmbedBuilder eb = new EmbedBuilder();
            eb.setColor(new Color(0x244aff));
            eb.setTitle(String.format(
                    "재생중 - %s [%s/%s] \n",
                    info.title,
                    formatTime(player.getPlayingTrack().getPosition()),
                    formatTime(player.getPlayingTrack().getDuration())
            ), player.getPlayingTrack().getInfo().uri);

            //1. tracklist 큐의 데이터를 리스트로 복제
            //2. list의 인덱스를 확인하여 문자열에 담음
            //3. Embed메시지를 생성하여 출력
            List playelist = new ArrayList(scheduler.getQueue());
            String str = "";
            long TotalDuration = player.getPlayingTrack().getDuration() - player.getPlayingTrack().getPosition();
            boolean overtext = false;
            if (playelist.size() != 0) {
                for (int i = 0; true; i++) {
                    if (playelist.size() == i) {
                        break;
                    }
                    AudioTrack t = (AudioTrack) playelist.get(i);
                    if(str.length() >= 900 && !overtext){
                        str += String.format(" ``[ +%d곡 ]`` ", playelist.size()-i);
                        overtext = true;
                        System.out.println(str.length());
                    }
                    String title = t.getInfo().title;
                    if(title.length() > 65) {
                         title = t.getInfo().title.substring(0, 65) + "...";
                    }
                    if(!overtext) {
                        str += String.format("%d. %s ``[%s]``\n", i + 1, title, formatTime(t.getInfo().length));
                    }
                    TotalDuration += t.getInfo().length;
                }
            } else {
                str = "None";
            }


            try {
                String tsplist = "";
                YoutubeDownloader yd = new YoutubeDownloader();
                YoutubeVideo video = yd.getVideo(player.getPlayingTrack().getIdentifier());
                String dsc = video.details().description();
                System.out.println(dsc);
                Pattern p1 = Pattern.compile("([0-9]{2}):([0-9]{2})((\\s*)?)(([\\S|\\s]*))(\\n?)");
                Pattern p2 = Pattern.compile("([0-9]{2}):([0-9]{2}):([0-9]{2})((\\s*)?)(([\\S|\\s]*))(\\n?)");
                Matcher m1 = p1.matcher(dsc); Matcher m2 = p2.matcher(dsc);
                while (m1.find()) {
                    tsplist += m1.group() + "\n";
                }
                eb.addField("TimeStemp", tsplist, false);

            }catch (Exception e) {
                e.printStackTrace();
            }

            eb.addField("TrackList", str, false);
            eb.setFooter(String.format("TotalDuration - %s", formatTime(TotalDuration)), null);
            event.getChannel().sendMessage(eb.build()).queue();
            raisePostCommand(event);
        }
    }

    public void gotoCommand(GenericMessageEvent event, String msg) {

        msg = msg.replaceFirst("goto ", "");

        //재생되고 있는 트랙이 있는지 확인
        if (this.player.getPlayingTrack() == null) {
            EmbedBuilder eb = new EmbedBuilder();
            eb.setColor(new Color(0xff6624));
            if (event instanceof MessageReceivedEvent) {
                MessageReceivedEvent msgEvent = (MessageReceivedEvent) event;
                eb.addField("경고 Warning", String.format(
                        "대기열이 비어 있습니다.\n``%s``",
                        msgEvent.getAuthor().getName().toString()
                ), false);
            } else if (event instanceof GenericMessageReactionEvent) {
                GenericMessageReactionEvent reactionEvent = (GenericMessageReactionEvent) event;
                eb.addField("경고 Warning", String.format(
                        "대기열이 비어 있습니다.\n``%s``",
                        reactionEvent.getUser().getName().toString()
                ), false);
            }
            event.getChannel().sendMessage(eb.build()).queue();
            return;
        } else { //비어있지 않으면

            //파라미터(시간 문자열)를 프레임단위로 환산하여 Position에 넣음
            long time = formatLong(msg);
            System.out.println(time);
            this.player.getPlayingTrack().setPosition(time);

        }
    }

    public void shuffleCommand(GenericMessageEvent event) {

        //재생되고 있는 트랙이 있는지 확인
        if (this.player.getPlayingTrack() == null) {
            return;
        }

        //큐를 가져옴, 빈 리스트(AudioTrack) 생성
        Queue queue = scheduler.getQueue();
        List<AudioTrack> list = new ArrayList<>();

        //큐에서 하나씩 빼서 리스트에 넣음
        for (int i = 0; true; i++) {
            list.add((AudioTrack) queue.poll());
            if (queue.size() == 0) {
                break;
            }
        }

        //리스트를 무작위로 섞음
        Collections.shuffle(list);

        //다시 큐에 넣음
        for (int i = 0; true; i++) {
            queue.offer(list.get(i));
            if (queue.size() == list.size()) {
                break;
            }
        }

        if (event instanceof MessageReceivedEvent) {
            MessageReceivedEvent msgEvent = (MessageReceivedEvent) event;
            msgEvent.getChannel().sendMessage(String.format("> 대기열 셔플 ``%s``", ((MessageReceivedEvent) event).getAuthor().getName())).queue();
        } else if (event instanceof GenericMessageReactionEvent) {
            GenericMessageReactionEvent reactionEvent = (GenericMessageReactionEvent) event;
            reactionEvent.getChannel().sendMessage(String.format("> 대기열 셔플 ``%s``", ((GenericMessageReactionEvent) event).getUser().getName())).queue();
        }

    }

    public void repeatCommand(GenericMessageEvent event) {

        //재생되고 있는 트랙이 있는지 확인
        if (this.player.getPlayingTrack() == null) {
            return;
        }

        //1. 현재 재생되고 있는 트랙의 url정보를 가져옴
        //2. url 정보를 커맨드 메시지처럼 사용하기 위해 수정함
        //3. playCommand 호출
        String msg = "play " + this.player.getPlayingTrack().getInfo().uri;
        playCommand(event, msg);

        if (event instanceof MessageReceivedEvent) {
            MessageReceivedEvent msgEvent = (MessageReceivedEvent) event;
            msgEvent.getChannel().sendMessage(String.format("> 현재곡 재등록 ``%s``", ((MessageReceivedEvent) event).getAuthor().getName())).queue();
        } else if (event instanceof GenericMessageReactionEvent) {
            GenericMessageReactionEvent reactionEvent = (GenericMessageReactionEvent) event;
            reactionEvent.getChannel().sendMessage(String.format("> 현재곡 재등록 ``%s``", ((GenericMessageReactionEvent) event).getUser().getName())).queue();
        }
    }

    public void clearCommand(GenericMessageEvent event, String dn) {
        dn = dn.replaceFirst("clear ", "");
        dn = dn.replaceFirst("cls ", "");
        dn = dn.replaceFirst("clr ", "");
        MessageReceivedEvent msgEvent = (MessageReceivedEvent) event;
        System.out.println(msgEvent.getChannel().getIterableHistory().complete().size());
        int i =0;
        for(Message msg : msgEvent.getChannel().getIterableHistory().complete()) {

            //if(((MessageReceivedEvent) event).getAuthor().getId().contains("606475718305775636")) {
                msgEvent.getChannel().deleteMessageById(msg.getId()).queue();
            //}
            i++;
            if(Integer.parseInt(dn)+1 == i){
                break;
            }
        }
    }

    public void papagoCommand(GenericMessageEvent event, String msg) {
        MessageReceivedEvent msgEvent = (MessageReceivedEvent) event;
        Papago papago = new Papago();
        String[] data = papago.manager(msg.replace("papago ", ""));
        String result = data[0]; String sourceLang = data[1]; String targetLang = data[2];

        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(new Color(0x1FFF2A));
        eb.setAuthor("Papago", "https://papago.naver.com/", "https://papago.naver.com/static/img/papago_og.png");
        eb.addField(String.format("[:flag_%s: -> :flag_%s:]", sourceLang, targetLang), result, false);
        event.getChannel().sendMessage(eb.build()).queue();
    }

    public void shoppingCommand(GenericMessageEvent event, String msg) {
        MessageReceivedEvent msgEvent = (MessageReceivedEvent) event;
        Shopping shop = new Shopping();
        EmbedBuilder eb = shop.manager(msg.replace("shopping ", "").replace("shop ", ""));
        event.getChannel().sendMessage(eb.build()).queue();
    }

    public void rouletteCommand(MessageReceivedEvent event, String msg) {
        msg = msg.replace("roulette ", "").replace("rol ", "");
        roulette.betting(event.getAuthor(), Integer.parseInt(msg));
        String[] r = roulette.letRoulette(event.getAuthor());
        int f = roulette.checkResult(event.getAuthor(), r);

        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(new Color(0xCF00FF));
        eb.setAuthor("Roulette");
        DecimalFormat form = new DecimalFormat("###,###,###,###,###,###,###,###,###,###,###");
        String rs = Arrays.toString(r).replaceAll("\\[", "").replaceAll("]", "");
        eb.addField(String.format("[%s] - %dx", rs, f), String.format("현재 금액 : %s", form.format(roulette.getMoney(event.getAuthor()))), false);
        event.getChannel().sendMessage(eb.build()).queue();

    }

    public void PlayingDisplay(MessageReceivedEvent event, String msg) {
        msg = msg.replaceFirst("PlayingDisplay ", "");
        updateServerIndex(event, "PlayDisplay", msg);
    }

    public void noticeCommand(MessageReceivedEvent event) {
        long[] chennals = {415133850079985677L, 542727744342196228L, 437280272182935562L, 607208059504427018L, 608664567928717322L, 637071354671136779L, 653562725913460738L, 660120286057725962L, 708730014492917820L};
        for(long c : chennals) {
            EmbedBuilder eb = new EmbedBuilder();
            eb.setColor(new Color(0x1BC3FF));
            eb.setAuthor("Notice",null,"https://cdn.discordapp.com/attachments/452403281428217856/609442329593643019/KIBAEMON-ICON.png");
            eb.setTitle("(전체공지) KIBAEMON MUSIC, 업데이트 안내");
            eb.setThumbnail("https://cdn.discordapp.com/attachments/452403281428217856/609441237228978254/KIBAEMON-LOGO.png");
            SimpleDateFormat format2 = new SimpleDateFormat ( "yyyy년 MM월dd일");
            Date time = new Date();
            eb.addField("업데이트 내용","봇 업데이트, 메시지 입출력 메커니즘 변경, 재생이 멋대로 스킵되던 버그 수정",true);
            eb.addField("기동일(오늘)",format2.format(time),true);
            eb.addField("라이브러리", "JDA(JAVA) v4.1.1_146", true);
            eb.addField("환경", String.format(
                    "OS: %s\nJAVA: %s\nJVM: %s\nJRE: %s\nCORE: %s\nMEMORY(BYTE): %s",
                    System.getProperty("os.name"),
                    System.getProperty("java.version"),
                    System.getProperty("java.vm.name"),
                    System.getProperty("java.specification.name"),
                    Runtime.getRuntime().availableProcessors(),
                    Runtime.getRuntime().freeMemory()
            ), true);
            eb.setFooter("KIBAEMON 2020.5 - K13A_ Laboratories", null);
            event.getJDA().getTextChannelById(c).sendMessage(eb.build()).queue();
        }
    }

    public void samsungCommand(MessageReceivedEvent event) {
        /*if(event.getAuthor().getId() != "173735165355360256" || event.getAuthor().getId() != "267563976475410443" || event.getAuthor().getId() != "231776579951263746" || event.getAuthor().getId() != "278760051210977280") {
            event.getChannel().sendMessage("> 해당 명령어는 제한 되었습니다.").queue();
            return;
        }*/
        try {
            Document doc = Jsoup.connect("https://finance.naver.com/item/main.nhn?code=005930").get();
            Element chart_img = doc.body().getElementById("img_chart_area");
            Element area = doc.body().getElementById("chart_area");
            Elements nums = area.getElementsByTag("em");



            String price = "";
            price = nums.get(0).children().html().split("\n")[0];
            /*for (Element e : nums.get(0).children() ) {
                price += e.html();
            }*/
            String ascent = "", ascent_per = "", yesterday = "", max = "", tradePoint = "", marketPrice = "", min = "", transePrice = "";

            //ascent = nums.get(1).children().html().split("\n")[0];
            for (Element e: nums.get(1).children()) {
                ascent += e.html();
            }
            ascent = ascent.substring(0, ascent.length()/2+1);
            //ascent_per = nums.get(2).children().html().split("\n")[0];
            for (Element e: nums.get(2).children()) {
                ascent_per += e.html();
            }
            ascent_per = ascent_per.substring(0, ascent_per.length()/2);
            yesterday = nums.get(3).children().html().split("\n")[0];
            max = nums.get(4).children().html().split("\n")[0];
            tradePoint = nums.get(5).children().html().split("\n")[0];
            marketPrice = nums.get(6).children().html().split("\n")[0];
            min = nums.get(7).children().html().split("\n")[0];
            transePrice = nums.get(8).children().html().split("\n")[0];

            Document doc2 = Jsoup.connect("http://hangang.dkserver.wo.tc/").get();

            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObj = (JSONObject) jsonParser.parse(doc2.body().html());

            float temp = Float.parseFloat(jsonObj.get("temp").toString());
            String date = jsonObj.get("time").toString();

            EmbedBuilder eb = new EmbedBuilder();
            eb.setColor(new Color(0x1271FF));
            eb.setAuthor("삼성 주식",null,"https://r1.community.samsung.com/t5/image/serverpage/image-id/99448i5E27FB2FC9981EEB/image-dimensions/240x240/image-coordinates/0%2C0%2C240%2C240?v=1.0");
            eb.setTitle("￦"+price);
            eb.setThumbnail("https://cdn.discordapp.com/attachments/452403281428217856/609441237228978254/KIBAEMON-LOGO.png");
            SimpleDateFormat format2 = new SimpleDateFormat ( "yyyy년 MM월dd일");
            Date time = new Date();
            eb.addField("전일 대비",ascent + " | " + ascent_per,true);
            eb.addField("전일", yesterday, true);
            eb.addField("시가", marketPrice, true);
            eb.addField("최고가", max, true);
            eb.addField("최저가", min, true);
            eb.addField("거래량", tradePoint, true);
            eb.addField("거래대금", transePrice, false);
            eb.addField("\n\n<한강수온>", "**" + temp + "°C** \n" + date + " 에 측정됨", true);
            eb.setFooter("KIBAEMON 2020", null);
            event.getChannel().sendMessage(eb.build()).queue();
            event.getChannel().sendMessage(chart_img.attr("src")).queue();
        } catch (Exception e) {

        }

    }

    public void hangangCommand(MessageReceivedEvent event) {
        try {
            Document doc = Jsoup.connect("http://hangang.dkserver.wo.tc/").get();

            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObj = (JSONObject) jsonParser.parse(doc.body().html());

            float temp = Float.parseFloat(jsonObj.get("temp").toString());
            String date = jsonObj.get("time").toString();

            event.getChannel().sendMessage("> 한강수온 : " + temp + "°C - " + date + "에 측정됨").queue();
        } catch (Exception e) { e.printStackTrace();  }

    }

    private static String SvSt_SettingDataQuery = "UPDATE ServerSetting SET %s='%s' WHERE id=%s;";
    /*
    == 서버 세팅 ==
    [1] 서버이름
    [2] 서버아이디
    [3] 재생표시 방법
    [4] 현재서버 재생볼륨
    ==============
     */
    public void updateServerIndex(MessageReceivedEvent event, String fieldName, String data) {
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:log.db");
            PreparedStatement preparedStatement = connection.prepareStatement(String.format(SvSt_SettingDataQuery, fieldName, data, event.getGuild().getId()));
            preparedStatement.executeUpdate();
        } catch (Exception e) {  }
    }

    // Long 타입 프레임을 String 타입 시간으로 환산
    private String formatTime(long time) {
        final long h = time / TimeUnit.HOURS.toMillis(1);
        final long m = time % TimeUnit.HOURS.toMillis(1) / TimeUnit.MINUTES.toMillis(1);
        final long s = time % TimeUnit.MINUTES.toMillis(1) / TimeUnit.SECONDS.toMillis(1);

        if (h != 0) {
            return String.format("%02d:%02d:%02d", h, m, s);
        } else {
            return String.format("%02d:%02d", m, s);
        }
    }

    // String 타입 시간을 Long 타입 프레임으로 환산
    private long formatLong(String msg) {
        String[] StrTime = msg.split(":");
        long LongTime = 0;
        if (StrTime.length == 3) {
            LongTime += Long.parseLong(StrTime[0]) * 60 * 60;
            LongTime += Long.parseLong(StrTime[1]) * 60;
            LongTime += Long.parseLong(StrTime[2]);
        } else if (StrTime.length == 2) {
            LongTime += Long.parseLong(StrTime[0]) * 60;
            LongTime += Long.parseLong(StrTime[1]);
        } else {
            LongTime += Long.parseLong(StrTime[1]);
        }

        return LongTime * 1000;
    }

    //URL여부 확인
    private boolean isURL(String Url) {
        try {
            new URL(Url);
            return true;

        } catch (MalformedURLException e) {
            return false;
        }
    }

    //유튜브 검색
    private ArrayList<String> searchYoutube(String title) {
        //유튜브 검색 경로 지정, 빈 해쉬맵 생성
        String youtubeUrl = "https://www.youtube.com/results?search_query=";
        ArrayList<String> video = new ArrayList<String>();
        System.out.println("[searchYoutube] Ready : " + youtubeUrl+title);
        //1. URL + title 로 검색하여 영상 제목을 전부 가져옴
        //2. video 해쉬맵에 하나씩 담아서 반환
        try {
            Document doc = Jsoup.connect(youtubeUrl+title).get();
            Elements titleE = doc.getElementsByTag("a");
            for(int i=0, j=0; titleE.size()> i; i++) {
                Element data = titleE.get(i);
                System.out.println("\n[" + i + "]TestParse: " + data.text() + "\n" +data.attr("href"));
                if( data.id().contains("video-title") && !data.attr("href").contains("http")) {

                    video.add("https://www.youtube.com" + data.attr("href"));
                    j++;
                }
            }
            return video;
        } catch (Exception e) {
            System.out.println(e);
            return video;
        }
    }
}
