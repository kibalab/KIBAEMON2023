package com.comduck.chatbot.discord;

import AudioCore.GuildMusicManager;
import AudioCore.PlayerManager;
import AudioCore.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.player.*;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.client.entities.Application;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.ShutdownEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.managers.AudioManager;

import java.awt.Color;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

public class DiscordBotMain extends ListenerAdapter {

    public int globalVolume = 10;

    public static void main(String[] args) throws Exception {
        new DiscordBotMain().start();
    }

    private void start() throws Exception {
        JDABuilder builder = new JDABuilder(AccountType.BOT);
        String token = "NjA2NDc1NzE4MzA1Nzc1NjM2.XULmjw.vYwYU3M816BsjuW-mXxXauGVVx4";
        builder.setToken(token);

        builder.addEventListener(this);
        builder.buildAsync();
    }

    @Override
    public void onShutdown(ShutdownEvent event) {
        super.onShutdown(event);
    }

    //리액션 수신 이벤트
    @Override
    public void onGenericMessageReaction(GenericMessageReactionEvent event) {
        super.onGenericMessageReaction(event);
        System.out.println(String.format("{'Type': 'Reaction', 'Guild_Name': '%s', 'Chennal_Name': '%s', 'Author': '%s', 'Reaction': '%s', 'Message_ID': '%s'}", event.getGuild().getName(), event.getChannel().getName(), event.getUser().getName(), event.getReaction(), event.getMessageId() ));
        System.out.println(event.getUser().isBot());
    }

    //메시지 수신 이벤트
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        //로그 출력
        System.out.println(String.format("{'Type': 'Message', 'Guild_Name': '%s', 'Chennal_Name': '%s', 'Author': '%s', 'Context': '%s'}", event.getGuild().getName(), event.getChannel().getName(), event.getAuthor().getName(), event.getMessage().getContentRaw() ));
        //System.out.println("[Log] " + event.getGuild().getName() + event.getAuthor().getName().getName() + " : " + event.getMessage().getContentDisplay());

        //봇 실행 스케쥴
        if( event.getAuthor().isBot() ){
            BotRunScheduler(event);
        }
        
        //커맨드 모음에 데이터 인풋
        boolean commandRun =  CommandInterface(event);
        if(!commandRun){
            //System.out.println( String.format( "{'Error': 'Unknown Command', 'Context': '%s'}", event.getMessage().getContentRaw() ) );
        }
    }

    private void BotRunScheduler(MessageReceivedEvent event){
        //event.getChannel().
    }

    //입력 데이터 처리
    private boolean CommandInterface(MessageReceivedEvent event) {
        //접두사 여부 식별
        String msg = "";
        if( !event.getMessage().getContentRaw().startsWith("?") )
        {return false;}
        else
        {
            msg = event.getMessage().getContentRaw();
            msg = msg.substring(1, msg.length());
        }
        if( event.getGuild().getName() == "Nerine force" )
        {
            if( event.getChannel().getName() != "bot-command" ){return false;}
        }
        BotCommands(event, msg);
        //명령어가 없을경우 false반환
        return false;
    }

    //명렁어
    private void BotCommands(final MessageReceivedEvent event, String msg)
    {

        if( msg.startsWith("test") )
        {
            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("OK", null);
            eb.setColor(new Color(0x244aff));
            eb.addField("Test Embed", "이 Embed 메시지는 다용도 테스트 메시지 입니다.", false);
            event.getChannel().sendMessage(eb.build()).queue();
            Message sendMsg = event.getMessage();
            wait_reaction(sendMsg, "⏹" );
        }
        else if( msg.startsWith("play") )
        {
            String url = msg.replaceFirst("play ", "");

            VoiceChannel Vch = event.getMember().getVoiceState().getChannel();
            AudioManager audiomng = event.getGuild().getAudioManager();
            audiomng.openAudioConnection(Vch);

            PlayerManager manager = PlayerManager.getInstance();
            manager.loadAndPlay(event, url);
            manager.getGuildMusicManager(event.getGuild()).player.setVolume(globalVolume);
        }
        else if( msg.startsWith("join") )
        {
            VoiceChannel Vch = event.getMember().getVoiceState().getChannel();
            if( event.getGuild().getName() == "Nerine force" )
            {
                if( Vch.getName() != "Music" ){return;}
            }
            event.getChannel().sendMessage(String.format(
                    "> %s 입장 ``%s``",
                    Vch.getName(),
                    event.getAuthor().getName()
            )).queue();
            AudioManager audiomng = event.getGuild().getAudioManager();
            audiomng.openAudioConnection(Vch);

        }
        else if( msg.startsWith("leave") )
        {

            VoiceChannel Vch = event.getGuild().getSelfMember().getVoiceState().getChannel();
            event.getChannel().sendMessage(String.format(
                    "> %s 퇴장 ``%s``",
                    Vch.getName(),
                    event.getAuthor().getName()
            )).queue();
            event.getGuild().getAudioManager().closeAudioConnection();

            PlayerManager manager = PlayerManager.getInstance();
            GuildMusicManager musicManager = manager.getGuildMusicManager(event.getGuild());
            AudioPlayer player = musicManager.player;
            TrackScheduler scheduler = musicManager.scheduler;

            scheduler.getQueue().clear();
            player.stopTrack();
            player.setPaused(false);

        }
        else if( msg.startsWith("stop") )
        {
            event.getChannel().sendMessage(String.format(
                    "> 대기열 재생 중지 ``%s``",
                    event.getAuthor().getName()
            )).queue();
            PlayerManager manager = PlayerManager.getInstance();
            GuildMusicManager musicManager = manager.getGuildMusicManager(event.getGuild());
            AudioPlayer player = musicManager.player;
            TrackScheduler scheduler = musicManager.scheduler;



            scheduler.getQueue().clear();
            player.stopTrack();
            player.setPaused(false);

        }
        else if( msg.startsWith("skip") )
        {
            PlayerManager manager = PlayerManager.getInstance();
            GuildMusicManager musicManager = manager.getGuildMusicManager(event.getGuild());
            AudioPlayer player = musicManager.player;
            TrackScheduler scheduler = musicManager.scheduler;

            if(player.getPlayingTrack() == null)
            {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setColor(new Color(0xff6624));
                eb.addField("경고 Warning", String.format(
                        "대기열이 비어 있습니다.\n``%s``",
                        event.getAuthor().getName().toString()
                ), false);
                event.getChannel().sendMessage(eb.build()).queue();
            }
            else
            {
                event.getChannel().sendMessage(String.format(
                        "> 곡 스킵 ``%s``",
                        event.getAuthor().getName()
                )).queue();
                scheduler.nextTrack();
            }


        }

        else if( msg.startsWith("volume") )
        {
            String _Nvol = msg.replaceFirst("volume ", "");
            PlayerManager manager = PlayerManager.getInstance();
            GuildMusicManager musicManager = manager.getGuildMusicManager(event.getGuild());
            AudioPlayer player = musicManager.player;

            int Ovol = player.getVolume();
            int Nvol = Integer.parseInt(_Nvol);

            if(Nvol > 100){
                Nvol = 100;
            }

            event.getChannel().sendMessage(String.format(
                    "> 음량 제어 %d->%s",
                    Ovol,
                    Nvol
            )).queue();
            globalVolume = Nvol;
            player.setVolume(globalVolume);
        }

        else if( msg.startsWith("tracklist") )
        {
            PlayerManager manager = PlayerManager.getInstance();
            GuildMusicManager musicManager = manager.getGuildMusicManager(event.getGuild());
            AudioPlayer player = musicManager.player;
            TrackScheduler scheduler = musicManager.scheduler;

            if(player.getPlayingTrack() == null){
                EmbedBuilder eb = new EmbedBuilder();
                eb.setColor(new Color(0xff6624));
                eb.addField("경고 Warning", String.format(
                        "대기열이 비어 있습니다.\n``%s``",
                        event.getAuthor().getName().toString()
                ), false);
                event.getChannel().sendMessage(eb.build()).queue();
            }
            else
            {
                AudioTrackInfo info = player.getPlayingTrack().getInfo();

                EmbedBuilder eb = new EmbedBuilder();
                eb.setColor(new Color(0x244aff));
                eb.setTitle(String.format(
                        "재생중 - %s [%s/%s]",
                        info.title,
                        formatTime(player.getPlayingTrack().getPosition()),
                        formatTime(player.getPlayingTrack().getDuration())
                ));
                List playelist = new ArrayList(scheduler.getQueue());
                String str = "";
                if(playelist.size() != 0) {
                    for (int i = 0;  true; i++) {
                        if ( playelist.size() == i )
                        {
                            break;
                        }
                        AudioTrack t = (AudioTrack) playelist.get(i);
                        str += String.format("%d. %s\n", i+1, t.getInfo().title);
                    }
                }
                else{str = "None";}
                eb.addField("TrackList", str, false);
                event.getChannel().sendMessage(eb.build()).queue();
            }
        }
    }

    private String formatTime(long time)
    {
        final long h = time / TimeUnit.HOURS.toMillis(1);
        final long m = time / TimeUnit.MINUTES.toMillis(1);
        final long s = time % TimeUnit.MINUTES.toMillis(1) / TimeUnit.SECONDS.toMillis(1);

        if(h != 0){
            return String.format("%2d:%2d:%2d", h, m, s);
        }
        else
        {
            return String.format("%2d:%2d", m, s);
        }

    }

    //미완성
    private void wait_reaction(Message msg, String emote)
    {
        msg.addReaction(emote).queue();
    }
}
