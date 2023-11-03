package com.comduck.chatbot.discord;

import com.comduck.chatbot.discord.audiocore.*;
import com.comduck.chatbot.discord.commands.CommandManager;
import com.sedmelluq.discord.lavaplayer.player.*;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.EmbedBuilder;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.*;

import java.sql.*;

public class DiscordBotMain extends ListenerAdapter implements PostCommandListener {

    Queue commandQueue = new LinkedList<GenericMessageEvent>();
    HashMap<String, OLD_CommandManager> commandManagerMap;

    public static void main(String[] args) throws Exception {
        StartArgumentCommand(args);
    }

    private void start(String bot) throws Exception {
        ResourceManager.loadAll();
        CommandManager.LoadAllCommands();

        ImageIO.scanForPlugins();
        commandManagerMap = new HashMap<>();
        JDABuilder builder = new JDABuilder(AccountType.BOT);

        JSONObject bots = (JSONObject) (new JSONParser().parse(new FileReader(new File("Bots.json"))));

        String token = (String)bots.get(bot);

        builder.setToken(token);
        builder.setActivity(Activity.playing("<가동중> ?help"));
        builder.addEventListeners(this);
        builder.build();
    }

    public static void StartArgumentCommand(String[] Arg) throws Exception {

        if(Arg[0].startsWith("-r") || Arg[0].startsWith("--run") ) {
            new DiscordBotMain().start(Arg[1]);
        } else if(Arg[0].startsWith("-b") || Arg[0].startsWith("--bots") ){
            JSONObject bots = (JSONObject) (new JSONParser().parse(new FileReader(new File("Bots.json"))));
            Iterator<String> keys = bots.keySet().iterator();
            System.out.println("[ Bots.js List ]");
            int i = 1;
            while(keys.hasNext()) {
                String key = keys.next();
                System.out.println(String.format("%d. %s : %s", i, key, bots.get(key)));
                i++;
            }
        } else {
            System.out.println("[ KIBATION2019-JAVA ]");
            System.out.println("[ K13A_Laboratories ]\n");
            System.out.println("-r  --run   {BotName}   : Login with Token in Bots.json.");
            System.out.println("-h  --help      : Print out help.");
            System.out.println("-b  --bot      : Output the bot list in Bots.json.");
        }
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        super.onGuildJoin(event);
        CommandManager.CreateInstance(event.getGuild());
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

        CommandManager.CreateInstance(event.getGuild());
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {

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

        for (BotInstance instance: CommandManager.Instances.values()) {
            instance.removePostCommandListener(this);
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

        //Pause
        if (event.getReactionEmote().getName().equals("⏯")) {
            CommandManager.ExcuteMessageCommend("pause", event, "");
        }
        //Stop
        if (event.getReactionEmote().getName().equals("⏹")) {
            CommandManager.ExcuteMessageCommend("stop", event, "");
        }
        //Skip
        if (event.getReactionEmote().getName().equals("⏭")) {
            CommandManager.ExcuteMessageCommend("skip", event, "");
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
            CommandManager.ExcuteMessageCommend("tracklist", event, "");
        }
        //Shuffle TrackList
        if (event.getReactionEmote().getName().equals("\uD83D\uDD00")) {
            CommandManager.ExcuteMessageCommend("shuffle", event, "");
        }
        //Repeat
        if (event.getReactionEmote().getName().equals("\uD83D\uDD02")) {
            CommandManager.ExcuteMessageCommend("repeat", event, "");
        }
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (!event.getUser().isBot()) {
            if (event.getReactionEmote().getName().equals("⭐")) {
                String Url = CommandManager.Instances.get(event.getGuild().getId()).player.getPlayingTrack().getInfo().uri;
                String Title = CommandManager.Instances.get(event.getGuild().getId()).player.getPlayingTrack().getInfo().title;

                AddFavoriteVideo(event, Url, Title);
            }
        }
    }

    @Override
    public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event) {
        if (!event.getUser().isBot()) {
            if (event.getReactionEmote().getName().equals("⭐")) {
                String Url = CommandManager.Instances.get(event.getGuild().getId()).player.getPlayingTrack().getInfo().uri;

                DeleteFavoriteVideo(event, Url);
            }
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
        /*
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
                connection.close();
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
                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
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

    private static String Rct_FavoriteAddQuery = "INSERT INTO FavoriteVideo(Title, Url, Server, UserID, Key) VALUES(?, ?, ?, ?, ?);";
    private static String Rct_FavoriteDeleQuery = "DELETE FROM FavoriteVideo WHERE Url=? AND Server=?;";

    public void AddFavoriteVideo(GenericMessageReactionEvent event, String Url, String Title){
        int Key = (int)(Math.random() * 10000);

        event.getChannel().sendMessage(String.format("> 현재곡 즐겨찾기 추가 ``%d`` ``%s``", Key, ((GenericMessageReactionEvent) event).getUser().getName())).queue();
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:log.db");

            PreparedStatement preparedStatement = connection.prepareStatement(Rct_FavoriteAddQuery);
            preparedStatement.setString(1, Title);
            preparedStatement.setString(2, Url);
            preparedStatement.setString(3, event.getGuild().getId());
            preparedStatement.setString(4, event.getUserId());
            preparedStatement.setInt(5, Key);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void DeleteFavoriteVideo(GenericMessageReactionEvent event, String Url){
        event.getChannel().sendMessage(String.format("> 현재곡 즐겨찾기 제거 ``%s``", ((GenericMessageReactionEvent) event).getUser().getName())).queue();
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:log.db");

            PreparedStatement preparedStatement = connection.prepareStatement(Rct_FavoriteDeleQuery);

            preparedStatement.setString(1, Url);
            preparedStatement.setString(2, event.getGuild().getId());
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
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

        if(event.getAuthor().getIdLong() == event.getJDA().getSelfUser().getIdLong()) {

            String msg = ((Message) commandQueue.poll()).getContentDisplay();
            System.out.println("[DiscordBotMain] Check Remoteable Command Message");
            if (msg.startsWith("?play") || msg.startsWith("tracklist") || msg.startsWith("songlist") || msg.startsWith("tlist") || msg.startsWith("tl") || msg.startsWith("slist") || msg.startsWith("queue") || msg.startsWith("q")) {
                wait_reaction(sendMsg, "⏯");//pause
                wait_reaction(sendMsg, "⏹");//stop
                wait_reaction(sendMsg, "⏭");//skip
                wait_reaction(sendMsg, "\uD83C\uDFA6");//printURL
                wait_reaction(sendMsg, "\uD83C\uDFB6");//tracklist
                wait_reaction(sendMsg, "\uD83D\uDD00");//Shuffle
                wait_reaction(sendMsg, "\uD83D\uDD02");//Repeat
                wait_reaction(sendMsg, "⭐");
                System.out.println("[DiscordBotMain] OnEnd Create Reaction Remote");
                ClearLastMessageReaction(event);
                CommandManager.Instances.get(event.getGuild().getId()).musicManager.lastPlayMessage = sendMsg;
                System.out.println("[DiscordBotMain] Update Last message");
                return true;
            }
        }
        return false;
    }

    private void ClearLastMessageReaction(MessageReceivedEvent event){
        Message last = CommandManager.Instances.get(event.getGuild().getId()).musicManager.lastPlayMessage;
        if(last != null) {
            last.clearReactions().queue();
            System.out.println("[DiscordBotMain] Remove Last message reactions");
        }
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
        if (event.getMessage().getEmotes().size() == 1 && event.getMessage().getContentRaw().startsWith("<") && event.getMessage().getContentRaw().endsWith(">") && event.getMessage().getGuild().getIdLong() != 102788723690700800L) { // 542727743909920798L
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

        msg = event.getMessage().getContentRaw();

        //GetChannelCommandManager(event).twitterCommand(event, msg);

        if (!event.getMessage().getContentRaw().startsWith("?")) {
            return false;
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

        String cmd = msg.split(" ")[0];
        CommandManager.ExcuteMessageCommend(cmd, event, msg);


        /*

        //커맨드 호출
        if (msg.startsWith("help") || msg.startsWith("hlp")) {
            GetChannelCommandManager(event).helpCommand(event);
        }else if (msg.startsWith("run -n")) {
            GetChannelCommandManager(event).noticeCommand(event);
        } else if (msg.startsWith("join")) {
            GetChannelCommandManager(event).joinCommand(event, msg);
            //cmdJoin(event);
        } else if (msg.startsWith("leave") || msg.startsWith("out")) {
            GetChannelCommandManager(event).leaveCommand(event);
            //cmdLeave(event);
        } else if (msg.startsWith("skip") || msg.startsWith("next")) {
            GetChannelCommandManager(event).skipCommand(event);
            //cmdSkip(event);
        } else if (msg.startsWith("volume") || msg.startsWith("vol")) {
            GetChannelCommandManager(event).volumeCommand(event, msg);
            //cmdVolume(event, msg);
        } else if (msg.startsWith("tracklist") || msg.startsWith("songlist") || msg.startsWith("tlist") || msg.startsWith("tl") || msg.startsWith("slist") || msg.startsWith("queue") || msg.startsWith("q")) {
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
        } else if (msg.startsWith("clip")) {
            GetChannelCommandManager(event).ClipCommand(event, msg);
        } else if (msg.startsWith("favorite")) {
            GetChannelCommandManager(event).favoriteCommand(event, msg);
        } else if (msg.startsWith("change")) {
            GetChannelCommandManager(event).ChangeFavKeyCommand(event, msg);
        } else if (msg.startsWith("samsung")) {
            GetChannelCommandManager(event).samsungCommand(event);
        }

         */
    }

    public OLD_CommandManager GetChannelCommandManager(MessageReceivedEvent event) {
        putMessageDB(event);

        return commandManagerMap.get(event.getGuild().getId());
    }

    //미완성
    private void wait_reaction(Message msg, String emote) {
        msg.addReaction(emote).queue();
        System.out.println(String.format("[DiscordBotMain] Set Reaction : %s to %s", emote, msg.getId()));
    }

    @Override
    public void onPostCommand(GenericMessageEvent genericMessageEvent) {
        try {
            MessageReceivedEvent msgEvent = (MessageReceivedEvent) genericMessageEvent;
            System.out.println(String.format("[DiscordBotMain] CommandQueue Add data: %s", msgEvent.getMessage().getContentRaw()));
            commandQueue.add(msgEvent.getMessage());
        } catch (Exception e) {

        }

    }

    //#endregion
}
