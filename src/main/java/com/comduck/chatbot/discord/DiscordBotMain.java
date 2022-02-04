package com.comduck.chatbot.discord;

import com.comduck.chatbot.discord.audiocore.*;
import com.sedmelluq.discord.lavaplayer.player.*;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceDeafenEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceSuppressEvent;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.EmbedBuilder;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import java.awt.Color;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

import java.sql.*;

public class DiscordBotMain extends ListenerAdapter implements PostCommandListener {

    Queue commandQueue = new LinkedList<GenericMessageEvent>();
    HashMap<String, CommandManager> commandManagerMap;

    public static void main(String[] args) throws Exception {
        new DiscordBotMain().start();
    }

    private void start() throws Exception {
        ImageIO.scanForPlugins();
        commandManagerMap = new HashMap<>();
        JDABuilder builder = new JDABuilder(AccountType.BOT);

        String token = "{Token Here}";
        builder.setToken(token);
        builder.setActivity(Activity.playing("<가동중> ?help"));
        builder.addEventListeners(this);
        builder.build();
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        super.onGuildJoin(event);
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
    }

    @Override
    public void onGuildReady(GuildReadyEvent event) {

        CreateServerIndex(event);
        String str = null;
        str = event.getGuild().getName() + "[" + event.getGuild().getId() + "]" + " : ";
        for(TextChannel c : event.getGuild().getTextChannels()) {
            str += c.getName() + "[" + c.getId() + "]" + " : " + c.canTalk() + " : {";
            for(Member m : c.getMembers()) {
                str += m.getNickname() + "(" + m.getUser().getName() + "#" + m.getUser().getId() + ")";
            }
             str += "}\n";
        }
        System.out.println(str);

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

            if(channel.getId().equals("--")) {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setColor(new Color(0x1BC3FF));
                eb.setAuthor("Ready!",null,"https://cdn.discordapp.com/attachments/452403281428217856/609442329593643019/KIBAEMON-ICON.png"); //
                eb.setTitle("KIBAEMON 준비완료!");
                eb.setThumbnail("https://cdn.discordapp.com/attachments/452403281428217856/609441237228978254/KIBAEMON-LOGO.png");
                SimpleDateFormat format2 = new SimpleDateFormat ( "yyyy년 MM월dd일");
                java.util.Date time = new java.util.Date();
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
                eb.setFooter("KIBAEMON 2019", null);
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

        System.out.println(String.format(
                "{'Type': 'LeaveVoice', 'Guild_Name': '%s#%s', 'VoiceChennal_Name': '%s#%s', 'User': '%s#%s'}",
                event.getGuild().getName(), event.getGuild().getId(),
                event.getChannelLeft().getName(), event.getChannelLeft().getId(),
                event.getMember().getUser().getName(), event.getMember().getUser().getId()
        ));

        if (event.getChannelLeft().getMembers().size() == 1) {
            try {
                if (event.getGuild().getAudioManager().getConnectedChannel().getId().equals(event.getChannelLeft().getId())) {
                    event.getGuild().getAudioManager().closeAudioConnection();
                }
            } catch (Exception e) {  }
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

        if (!event.getUser().isBot()) return;

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

    public final static String MSG_DATABASE = "log.db";
    private static String Msg_logDataQuery = "INSERT INTO Message(Type, Guild_Name, Chennal_Name, Author, MessageID, Context, Date) VALUES(?, ?, ?, ?, ?, ?, strftime('%Y-%m-%d %H:%M:%f', 'now', 'localtime'));";
    private static String Rct_logDataQuery = "INSERT INTO Reaction(Type, Guild_Name, Chennal_Name, Author, MessageID, Emote, Date) VALUES(?, ?, ?, ?, ?, ?, strftime('%Y-%m-%d %H:%M:%f', 'now', 'localtime'));";

    //Class.forName("org.sqlite.JDBC"); 모듈이 있는지 검사
    public void putMessageDB(GenericMessageEvent event) {
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:log.db");

            if(event instanceof MessageReceivedEvent) {
                //"{'Type': 'Message#%s', 'Guild_Name': '%s#%s', 'Chennal_Name': '%s#%s', 'Author': '%s#%s', 'MessageID': '%s', 'Context': '%s'}"
                MessageReceivedEvent msgEvent = (MessageReceivedEvent) event;
                PreparedStatement preparedStatement = connection.prepareStatement(Msg_logDataQuery);
                preparedStatement.setString(1, "Message");
                preparedStatement.setString(2, msgEvent.getGuild().getName() + '#' + msgEvent.getGuild().getId());
                preparedStatement.setString(3, msgEvent.getChannel().getName() + '#' + msgEvent.getChannel().getId());
                preparedStatement.setString(4, msgEvent.getAuthor().getName() + '#' + msgEvent.getAuthor().getId());
                preparedStatement.setString(5, msgEvent.getMessage().getId());
                preparedStatement.setString(6, msgEvent.getMessage().getContentRaw());
                preparedStatement.executeUpdate();
            } else {
                //"{'Type': 'Reaction', 'Guild_Name': '%s#%s', 'Chennal_Name': '%s#%s', 'Author': '%s#%s', 'MessageID': '%s', 'Emote': '%s'}"
                GenericMessageReactionEvent reactionEvent = (GenericMessageReactionEvent) event;
                PreparedStatement preparedStatement = connection.prepareStatement(Rct_logDataQuery);
                preparedStatement.setString(1, "Reaction");
                preparedStatement.setString(2, reactionEvent.getGuild().getName() + '#' + reactionEvent.getGuild().getId());
                preparedStatement.setString(3, reactionEvent.getChannel().getName() + '#' + reactionEvent.getChannel().getId());
                preparedStatement.setString(4, reactionEvent.getUser().getName() + '#' + reactionEvent.getUser().getId());
                preparedStatement.setString(5, reactionEvent.getMessageId());
                preparedStatement.setString(6, reactionEvent.getReactionEmote().toString());
                preparedStatement.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String SvSt_SettingDataQuery = "INSERT INTO ServerSetting(Name, ID, PlayDisplay, PlayVolume) VALUES(?, ?, ?, ?);";
    /*
    == 서버 세팅 ==
    [1] 서버이름
    [2] 서버아이디
    [3] 재생표시 방법
    [4] 현재서버 재생볼륨
    ==============
     */
    public void CreateServerIndex(GuildReadyEvent event) {
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:log.db");
            Statement statement = connection.createStatement();
            PreparedStatement preparedStatement = connection.prepareStatement(SvSt_SettingDataQuery);
            preparedStatement.setString(1, event.getGuild().getName());
            preparedStatement.setString(2, event.getGuild().getId());
            preparedStatement.setString(3, "0");
            preparedStatement.setString(4, "30");
            preparedStatement.executeUpdate();
        } catch (Exception e) {  }
    }

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
        if (commandRun) {
            System.out.println( String.format( "{'Error': 'Unknown Command', 'Context': '%s'}", event.getMessage().getContentRaw() ) );
            }
        }

    private boolean reactionInterface(MessageReceivedEvent event) {
        Message sendMsg = event.getMessage();
        String msg = ((Message) commandQueue.poll()).getContentDisplay();
        System.out.println(msg);
        if(event.getAuthor().isBot()) {
            if (msg.startsWith("?play") || msg.startsWith("?tracklist") || msg.startsWith("songlist") || msg.startsWith("tlist") || msg.startsWith("slist")) {
                wait_reaction(sendMsg, "⏯");//pause
                wait_reaction(sendMsg, "⏹");//stop
                wait_reaction(sendMsg, "⏭");//skip
                wait_reaction(sendMsg, "\uD83C\uDFA6");//printURL
                wait_reaction(sendMsg, "\uD83C\uDFB6");//tracklist
                wait_reaction(sendMsg, "\uD83D\uDD00");//Shuffle
                wait_reaction(sendMsg, "\uD83D\uDD02");//Repeat
                return true;
            }
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
        if(event.getChannel().getId().contains("694734337647706123")) { // 우리집

            event.getJDA().getGuildById("542727743909920798").getTextChannelById("542727744342196228").sendMessage(event.getMessage().getContentRaw()).queue();
        }
        if(event.getChannel().getId().contains("708914009385992194")) { // 가상시

            event.getJDA().getGuildById("665080322085617665").getTextChannelById("665098737420337156").sendMessage(event.getMessage().getContentRaw()).queue();
        }
        String msg = "";
        if (event.getMessage().getEmotes().size() == 1 && event.getMessage().getContentRaw().startsWith("<") && event.getMessage().getContentRaw().endsWith(">")) { // && event.getMessage().getGuild().getIdLong() != 542727743909920798L
            String emojiUrl = event.getMessage().getEmotes().get(0).getImageUrl();
            User user  = event.getMessage().getAuthor();

            event.getMessage().delete().queue();

            EmbedBuilder eb = new EmbedBuilder();
            eb.setAuthor(user.getName(), user.getAvatarUrl(), user.getAvatarUrl());
            eb.setImage(emojiUrl);
            eb.setColor(new Color(0x244aff));
            event.getChannel().sendMessage(eb.build()).queue();
            System.out.print("BigEmoji Pring OK");
        }
        if (!event.getMessage().getContentRaw().startsWith("?")) {
            return false;
        } else {
            msg = event.getMessage().getContentRaw();
        }
        if (event.getGuild().getName() == "Nerine force") {
            if (event.getChannel().getName() != "bot-command") {
                return false;
            }
        }
        for(String cmd : msg.split("\\n")) {
            cmd = cmd.substring(1, cmd.length());
            botCommands(event, cmd);
        }

        //명령어가 없을경우 false반환
        return true;
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
            GetChannelCommandManager(event).helpCommand(event);
        }else if (msg.startsWith("run -n")) {
            GetChannelCommandManager(event).noticeCommand(event);
        } else if (msg.startsWith("test")) {
            cmdTest(event);
        } else if (msg.startsWith("play")) {
            GetChannelCommandManager(event).playCommand(event, msg);
            //cmdPlay(event, msg);
        } else if (msg.startsWith("pause")) {
            GetChannelCommandManager(event).pauseCommand(event);
            //cmdPause(event);
        } else if (msg.startsWith("join")) {
            GetChannelCommandManager(event).joinCommand(event, msg);
            //cmdJoin(event);
        } else if (msg.startsWith("leave") || msg.startsWith("out")) {
            GetChannelCommandManager(event).leaveCommand(event);
            //cmdLeave(event);
        } else if (msg.startsWith("stop")) {
            GetChannelCommandManager(event).stopCommand(event);
            //cmdStop(event);
        } else if (msg.startsWith("skip") || msg.startsWith("next")) {
            GetChannelCommandManager(event).skipCommand(event);
            //cmdSkip(event);
        } else if (msg.startsWith("volume") || msg.startsWith("vol")) {
            GetChannelCommandManager(event).volumeCommand(event, msg);
            //cmdVolume(event, msg);
        } else if (msg.startsWith("tracklist") || msg.startsWith("songlist") || msg.startsWith("tlist") || msg.startsWith("slist")) {
            GetChannelCommandManager(event).tracklistCommand(event);
            //cmdTrackList(event);
        } else if (msg.startsWith("goto")) {
            GetChannelCommandManager(event).gotoCommand(event, msg);
            //cmdGoTo(event, msg);
        } else if (msg.startsWith("shuffle") || msg.startsWith("mix") || msg.startsWith("sf")) {
            GetChannelCommandManager(event).shuffleCommand(event);
            //cmdShuffle(event, msg);
        } else if (msg.startsWith("repeat") || msg.startsWith("replay") || msg.startsWith("rp")) {
            GetChannelCommandManager(event).repeatCommand(event);
            //cmdRepeat(event);
        } else if (msg.startsWith("clear") || msg.startsWith("clr") || msg.startsWith("cls")) {
            GetChannelCommandManager(event).clearCommand(event, msg);
        } else if (msg.startsWith("papago")) {
            GetChannelCommandManager(event).papagoCommand(event, msg);
        } else if (msg.startsWith("shopping") || msg.startsWith("shop")) {
            GetChannelCommandManager(event).shoppingCommand(event, msg);
        } else if (msg.startsWith("roulette") || msg.startsWith("rol")) {
            GetChannelCommandManager(event).rouletteCommand(event, msg);
        } else if (msg.startsWith("PlayingDisplay")) {
            GetChannelCommandManager(event).PlayingDisplay(event, msg);
        } else if (msg.startsWith("samsung")) {
            GetChannelCommandManager(event).samsungCommand(event);
        } else if (msg.startsWith("hangang")) {
            GetChannelCommandManager(event).hangangCommand(event);
        }
    }

    public CommandManager GetChannelCommandManager(MessageReceivedEvent event) {
        putMessageDB(event);

        return commandManagerMap.get(event.getGuild().getId());
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
        try {
            MessageReceivedEvent msgEvent = (MessageReceivedEvent) genericMessageEvent;
            System.out.println(String.format("CommandQueue Add data: %s", msgEvent.getMessage().getContentRaw()));
            commandQueue.add(msgEvent.getMessage());
        } catch (Exception e) {

        }

    }

    //#endregion
}
