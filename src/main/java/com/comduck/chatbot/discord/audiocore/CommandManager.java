package com.comduck.chatbot.discord.audiocore;

import com.comduck.chatbot.discord.naverapi.Papago;
import com.comduck.chatbot.discord.naverapi.Shopping;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.GenericMessageEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.core.managers.AudioManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.awt.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CommandManager {


    GuildMusicManager musicManager; // = manager.getGuildMusicManager(event.getGuild());
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
    private void raisePostCommand(GenericMessageEvent event) {
        for (PostCommandListener listener : postCommandListeners) {
            listener.onPostCommand(event);
        }
    }

    public void helpCommand(GenericMessageEvent event) {
        String msg = "***임시 명령어 도움말***\r"+
"```접두사 : '?' (명령어를 사용할때 가장앞에 반드시 포함되어있어야 합니다)\r"+
"help : 명령어 도움말 입니다. [hlp로 대체가능]\r"+
"Music\r"+
"    play [Url또는Title] : 음악을 재생합니다.\r"+
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
"```\r"+
"제작 : KIBA\r"+
"도움 : JUNG ";
        event.getChannel().sendMessage(msg).queue();
    }

    public void playCommand(GenericMessageEvent event, String msg) {
        VoiceChannel Vch = null;
        String url = msg.replaceFirst("play", "").replace(" ", "");

        // GenericMessageEvent 종속 메소드 분리
        if (event instanceof MessageReceivedEvent) {
            MessageReceivedEvent msgEvent = (MessageReceivedEvent) event;

            Vch = ((MessageReceivedEvent) event).getMember().getVoiceState().getChannel();
        } else if (event instanceof GenericMessageReactionEvent) {
            GenericMessageReactionEvent reactionEvent = (GenericMessageReactionEvent) event;

            Vch = ((GenericMessageReactionEvent) event).getMember().getVoiceState().getChannel();
        }


        AudioManager audiomng = event.getGuild().getAudioManager();
        if (!audiomng.isConnected()) {
            audiomng.openAudioConnection(Vch);
        }
        PlayerManager manager = PlayerManager.getInstance();

        //1. 파라미터 없으면 현재 재생되고 있는 음액 재신청
        //2. URL이 아니면 유튜브에 검색
        if(url.equals("")) {
            url = this.player.getPlayingTrack().getInfo().uri;
        } else if (!isURL(url) && !url.startsWith("ytsearch:")) {
            ArrayList<String> video = searchYoutube(url);
            url = video.get(0);
        }


        //음악 재생
        manager.loadAndPlay(event, url);
        manager.getGuildMusicManager(event.getGuild()).player.setVolume(globalVolume);
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
                    "재생중 - %s [%s/%s]",
                    info.title,
                    formatTime(player.getPlayingTrack().getPosition()),
                    formatTime(player.getPlayingTrack().getDuration())
            ));

            //1. tracklist 큐의 데이터를 리스트로 복제
            //2. list의 인덱스를 확인하여 문자열에 담음
            //3. Embed메시지를 생성하여 출력
            List playelist = new ArrayList(scheduler.getQueue());
            String str = "";
            long TotalDuration = player.getPlayingTrack().getDuration() - player.getPlayingTrack().getPosition();
            if (playelist.size() != 0) {
                for (int i = 0; true; i++) {
                    if (playelist.size() == i) {
                        break;
                    }
                    AudioTrack t = (AudioTrack) playelist.get(i);
                    str += String.format("%d. %s ``[%s]``\n", i + 1, t.getInfo().title, formatTime(t.getInfo().length));
                    TotalDuration += t.getInfo().length;
                }
            } else {
                str = "None";
            }
            eb.addField("TrackList", str, false);
            eb.setFooter(String.format("TotalDuration - %s", formatTime(TotalDuration)), null);
            event.getChannel().sendMessage(eb.build()).queue();
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

    public void clearCommand(GenericMessageEvent event) {
        MessageReceivedEvent msgEvent = (MessageReceivedEvent) event;
        System.out.println(msgEvent.getChannel().getIterableHistory().complete().size());
        for(Message msg : msgEvent.getChannel().getIterableHistory().complete()) {

            msgEvent.getChannel().deleteMessageById(msg.getId()).queue(  );
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

    // Long 타입 프레임을 String 타입 시간으로 환산
    private String formatTime(long time) {
        final long h = time / TimeUnit.HOURS.toMillis(1);
        final long m = time % TimeUnit.HOURS.toMillis(1) / TimeUnit.MINUTES.toMillis(1);
        final long s = time % TimeUnit.MINUTES.toMillis(1) / TimeUnit.SECONDS.toMillis(1);

        if (h != 0) {
            return String.format("%2d:%2d:%2d", h, m, s);
        } else {
            return String.format("%2d:%2d", m, s);
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

        //1. URL + title 로 검색하여 영상 제목을 전부 가져옴
        //2. video 해쉬맵에 하나씩 담아서 반환
        try {
            Document doc = Jsoup.connect(youtubeUrl+title).get();
            Elements titleE = doc.getElementsByTag("a").select("a[title]");
            for(int i=0, j=0; titleE.size()> i; i++) {
                Element data = titleE.get(i);
                if( data.classNames().contains("yt-uix-tile-link") && !data.attr("href").contains("http")) {
                    //System.out.println("\n[" + i + "]TestParse: " + data.text() + "\n" +data.attr("href"));
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
