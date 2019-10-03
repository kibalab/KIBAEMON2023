package com.comduck.chatbot.discord;

import com.comduck.chatbot.discord.audiocore.*;
import com.sedmelluq.discord.lavaplayer.player.*;
import net.dv8tion.jda.client.events.call.voice.CallVoiceLeaveEvent;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.ShutdownEvent;
import net.dv8tion.jda.core.events.guild.GuildJoinEvent;
import net.dv8tion.jda.core.events.guild.GuildReadyEvent;
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.core.events.message.GenericMessageEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.EmbedBuilder;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.*;

public class DiscordBotMain extends ListenerAdapter implements PostCommandListener {

    Queue commandQueue = new LinkedList<GenericMessageEvent>();
    HashMap<String, CommandManager> commandManagerMap;

    public static void main(String[] args) throws Exception {
        new DiscordBotMain().start();
    }

    private void start() throws Exception {
        commandManagerMap = new HashMap<>();
        JDABuilder builder = new JDABuilder(AccountType.BOT);
        String token = "NjA2NDc1NzE4MzA1Nzc1NjM2.XULmjw.vYwYU3M816BsjuW-mXxXauGVVx4";
        builder.setToken(token);

        builder.addEventListener(this);
        builder.buildAsync();
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        super.onGuildJoin(event);

    }

    @Override
    public void onGuildReady(GuildReadyEvent event) {

        PlayerManager manager = PlayerManager.getInstance();
        GuildMusicManager musicManager = manager.getGuildMusicManager(event.getGuild());

        /*
        나중에 이거로 고칠것.
        GuildMusicManager musicManager = new GuildMusicManager();
         */

        AudioPlayer player = musicManager.player;
        TrackScheduler scheduler = musicManager.scheduler;

        CommandManager cmdManager = new CommandManager(musicManager, player, scheduler);
        cmdManager.addPostCommandListener(this);

        commandManagerMap.put(event.getGuild().getId(), cmdManager);
        //onReadyMessage(event);
    }

    public void onReadyMessage(GuildReadyEvent event) {
        for(TextChannel channel : event.getGuild().getTextChannels()) {
            boolean channelTrue = false;
            //channelTrue = channel.getId().equals("607208059504427018"); // Nerine Force - bot_command
            //channelTrue = channel.getId().equals("424887201281605661"); // LucidLab - 명령어

            if(channel.getId().equals("558886994676285443")) {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setColor(new Color(0x1BC3FF));
                eb.setAuthor("Ready!",null,"https://cdn.discordapp.com/attachments/452403281428217856/609442329593643019/KIBAEMON-ICON.png"); //
                eb.setTitle("KIBAEMON 준비완료!");
                eb.setThumbnail("https://cdn.discordapp.com/attachments/452403281428217856/609441237228978254/KIBAEMON-LOGO.png");
                SimpleDateFormat format2 = new SimpleDateFormat ( "yyyy년 MM월dd일");
                Date time = new Date();
                eb.addField("기동일(오늘)",format2.format(time),true);
                eb.addField("라이브러리", "JDA(JAVA)", true);
                eb.addField("환경", String.format(
                        "OS: %s\nJAVA: %s\nJVM: %s\nJRE: %s\nCORE: %s\nMEMORY(BYTE): %s",
                        System.getProperty("os.name"),
                        System.getProperty("java.version"),
                        System.getProperty("java.vm.name"),
                        System.getProperty("java.specification.name"),
                        Runtime.getRuntime().availableProcessors(),
                        Runtime.getRuntime().freeMemory()
                ), true);
                eb.setFooter("KIBAEMON 2019 in JBot Project", null);
                channel.sendMessage(eb.build()).queue();
            }
        }

    }

    @Override
    public void onShutdown(ShutdownEvent event) {
        super.onShutdown(event);

        for (CommandManager cmdManager : commandManagerMap.values()) {
            cmdManager.removePostCommandListener(this);
        }
    }


    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        super.onGuildVoiceLeave(event);

        if (event.getGuild().getAudioManager().getConnectedChannel() != null) {
            return;
        } else if (event.getChannelLeft() != null) {
            return;
        }


        if (event.getChannelLeft().getMembers().size() <= 1) {
            if(event.getGuild().getAudioManager().getConnectedChannel().getIdLong() == event.getChannelLeft().getIdLong()) {
                event.getGuild().getAudioManager().closeAudioConnection();
            }
        }
    }

    /**
     * 리액션 수신 이벤트
     *
     * @param event
     */
    @Override
    public void onGenericMessageReaction(GenericMessageReactionEvent event) {
        super.onGenericMessageReaction(event);
        System.out.println(String.format(
                "{'Type': 'Reaction', 'Guild_Name': '%s#%s', 'Chennal_Name': '%s#%s', 'Author': '%s#%s', 'MessageID': '%s', 'Emote': '%s'}",
                event.getGuild().getName(), event.getGuild().getId(),
                event.getChannel().getName(), event.getChannel().getId(),
                event.getUser().getName(), event.getUser().getId(),
                event.getMessageId(),
                event.getReactionEmote().toString()
        ));
        if (!event.getUser().isBot()) {
            onReactionBindCommand(event);
        }
    }

    /**
     * 리액션 커맨드
     *
     * @param event
     */
    private void onReactionBindCommand(GenericMessageReactionEvent event) {
        //Pause
        if (event.getReactionEmote().getName().equals("⏯")) {
            commandManagerMap.get(event.getGuild().getId()).pauseCommand(event);
        }
        //Stop
        if (event.getReactionEmote().getName().equals("⏹")) {
            commandManagerMap.get(event.getGuild().getId()).stopCommand(event);
        }
        //Skip
        if (event.getReactionEmote().getName().equals("⏭")) {
            commandManagerMap.get(event.getGuild().getId()).skipCommand(event);
        }
        //printURL
        if (event.getReactionEmote().getName().equals("🎦")) {
            PlayerManager manager = PlayerManager.getInstance();
            GuildMusicManager musicManager = manager.getGuildMusicManager(event.getGuild());
            AudioPlayer player = musicManager.player;
            TrackScheduler scheduler = musicManager.scheduler;

            event.getChannel().sendMessage(String.format("> %s", player.getPlayingTrack().getInfo().uri)).queue();
        }
        //TrackList
        if (event.getReactionEmote().getName().equals("\uD83C\uDFB6")) {
            commandManagerMap.get(event.getGuild().getId()).tracklistCommand(event);
        }
        //Shuffle TrackList
        if (event.getReactionEmote().getName().equals("\uD83D\uDD00")) {
            commandManagerMap.get(event.getGuild().getId()).shuffleCommand(event);
        }
        //Repeat
        if (event.getReactionEmote().getName().equals("\uD83D\uDD02")) {
            commandManagerMap.get(event.getGuild().getId()).repeatCommand(event);
        }
    }

    /**
     * 메시지 수신 이벤트
     *
     * @param event
     */
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        //로그 출력
        System.out.println(String.format(
                "{'Type': 'Message#%s', 'Guild_Name': '%s#%s', 'Chennal_Name': '%s#%s', 'Author': '%s#%s', 'Context': '%s'}",
                event.getMessage().getId(),
                event.getGuild().getName(), event.getGuild().getId(),
                event.getChannel().getName(), event.getChannel().getId(),
                event.getAuthor().getName(), event.getAuthor().getId(),
                event.getMessage().getContentRaw()
        ));
        //System.out.println("[Log] " + event.getGuild().getName() + event.getAuthor().getName().getName() + " : " + event.getMessage().getContentDisplay());

        if (event.getAuthor().isBot()) {
            if (commandQueue.size() >= 1) {
                System.out.println(true);
                reactionInterface(event);
            }
        }

        //커맨드 모음에 데이터 인풋
        boolean commandRun = commandInterface(event);
        if (!commandRun) {
            //System.out.println( String.format( "{'Error': 'Unknown Command', 'Context': '%s'}", event.getMessage().getContentRaw() ) );
        }
    }

    private boolean reactionInterface(MessageReceivedEvent event) {
        Message sendMsg = event.getMessage();
        String msg = ((Message) commandQueue.poll()).getContentDisplay();
        System.out.println(msg);
        if (msg.startsWith("?play")) {
            wait_reaction(sendMsg, "⏯");//pause
            wait_reaction(sendMsg, "⏹");//stop
            wait_reaction(sendMsg, "⏭");//skip
            wait_reaction(sendMsg, "\uD83C\uDFA6");//printURL
            wait_reaction(sendMsg, "\uD83C\uDFB6");//tracklist
            wait_reaction(sendMsg, "\uD83D\uDD00");//Shuffle
            wait_reaction(sendMsg, "\uD83D\uDD02");//Repeat
            return true;
        }
        return false;
    }

    /**
     * 입력 데이터 처리
     *
     * @param event
     * @return
     */
    private boolean commandInterface(MessageReceivedEvent event) {
        //접두사 여부 식별
        String msg = "";
        if (!event.getMessage().getContentRaw().startsWith("?")) {
            return false;
        } else {
            msg = event.getMessage().getContentRaw();
            msg = msg.substring(1, msg.length());
        }
        if (event.getGuild().getName() == "Nerine force") {
            if (event.getChannel().getName() != "bot-command") {
                return false;
            }
        }
        botCommands(event, msg);
        //명령어가 없을경우 false반환
        return false;
    }

    /**
     * 명령어 실행
     *
     * @param event
     * @param msg
     */
    private void botCommands(final MessageReceivedEvent event, String msg) {

        PlayerManager manager = PlayerManager.getInstance();
        GuildMusicManager musicManager = manager.getGuildMusicManager(event.getGuild());
        AudioPlayer player = musicManager.player;
        TrackScheduler scheduler = musicManager.scheduler;

        //커맨드 호출
        if (msg.startsWith("help") || msg.startsWith("hlp")) {
            commandManagerMap.get(event.getGuild().getId()).helpCommand(event);
        } else if (msg.startsWith("test")) {
            cmdTest(event);
        } else if (msg.startsWith("play")) {
            commandManagerMap.get(event.getGuild().getId()).playCommand(event, msg);
            //cmdPlay(event, msg);
        } else if (msg.startsWith("pause")) {
            commandManagerMap.get(event.getGuild().getId()).pauseCommand(event);
            //cmdPause(event);
        } else if (msg.startsWith("join")) {
            commandManagerMap.get(event.getGuild().getId()).joinCommand(event, msg);
            //cmdJoin(event);
        } else if (msg.startsWith("leave") || msg.startsWith("out")) {
            commandManagerMap.get(event.getGuild().getId()).leaveCommand(event);
            //cmdLeave(event);
        } else if (msg.startsWith("stop")) {
            commandManagerMap.get(event.getGuild().getId()).stopCommand(event);
            //cmdStop(event);
        } else if (msg.startsWith("skip") || msg.startsWith("next")) {
            commandManagerMap.get(event.getGuild().getId()).skipCommand(event);
            //cmdSkip(event);
        } else if (msg.startsWith("volume") || msg.startsWith("vol")) {
            commandManagerMap.get(event.getGuild().getId()).volumeCommand(event, msg);
            //cmdVolume(event, msg);
        } else if (msg.startsWith("tracklist") || msg.startsWith("songlist") || msg.startsWith("tlist") || msg.startsWith("slist")) {
            commandManagerMap.get(event.getGuild().getId()).tracklistCommand(event);
            //cmdTrackList(event);
        } else if (msg.startsWith("goto")) {
            commandManagerMap.get(event.getGuild().getId()).gotoCommand(event, msg);
            //cmdGoTo(event, msg);
        } else if (msg.startsWith("shuffle") || msg.startsWith("mix") || msg.startsWith("sf")) {
            commandManagerMap.get(event.getGuild().getId()).shuffleCommand(event);
            //cmdShuffle(event, msg);
        } else if (msg.startsWith("repeat") || msg.startsWith("replay") || msg.startsWith("rp")) {
            commandManagerMap.get(event.getGuild().getId()).repeatCommand(event);
            //cmdRepeat(event);
        } else if (msg.startsWith("clear") || msg.startsWith("clr") || msg.startsWith("cls")) {
            commandManagerMap.get(event.getGuild().getId()).clearCommand(event);
        }
    }

    //#region 명령어 함수
    //명령어 함수////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //유일하게 메인에 남은 명령어 ( 테스트용 )
    private boolean cmdTest(MessageReceivedEvent event) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("OK", null);
        eb.setColor(new Color(0x244aff));
        eb.addField("Test Embed", "이 Embed 메시지는 다용도 테스트 메시지 입니다.", false);
        event.getChannel().sendMessage(eb.build()).queue();
        Message sendMsg = event.getMessage();
        wait_reaction(sendMsg, "⏹");
        return true;
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //미완성
    private void wait_reaction(Message msg, String emote) {
        msg.addReaction(emote).queue();
    }

    @Override
    public void onPostCommand(GenericMessageEvent genericMessageEvent) {
        MessageReceivedEvent msgEvent = (MessageReceivedEvent) genericMessageEvent;
        System.out.println(String.format("CommandQueue Add data: %s", msgEvent.getMessage().getContentRaw()));
        commandQueue.add(msgEvent.getMessage());
    }

    //#endregion
}
