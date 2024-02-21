package com.comduck.chatbot.discord;

import com.comduck.chatbot.discord.audiocore.*;
import com.sedmelluq.discord.lavaplayer.player.*;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
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
import org.reflections.Reflections;
import org.mariadb.jdbc.*;
import se.michaelthelin.spotify.SpotifyApi;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.*;

import java.sql.*;

public class DiscordBotMain extends ListenerAdapter implements PostCommandListener {
    static public SpotifyApi spotifyApi = new SpotifyApi.Builder().setClientId("ee42dee9338d44b5a1dba476c5e75055").setClientSecret("da310a9627714fdfa2869742ed022986").build();

    Queue commandQueue = new LinkedList<GenericMessageEvent>();

    public static void main(String[] args) throws Exception {
        StartArgumentCommand(args);
    }

    private void start(String bot) throws Exception {

        ResourceManager.loadAll();
        CommandManager.LoadAllCommands();
        ProcessorManager.LoadAllCommands();

        ImageIO.scanForPlugins();
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
                System.out.printf("%d. %s : %s%n", i, key, bots.get(key));
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
        new BotInstance(event.getGuild(), spotifyApi);
    }

    @Override
    public void onGuildReady(GuildReadyEvent event) {
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

        new BotInstance(event.getGuild(), spotifyApi);
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
    }


    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        super.onGuildVoiceLeave(event);

        System.out.printf(
                "{'Type': 'LeaveVoice', 'Guild_Name': '%s#%s', 'VoiceChennal_Name': '%s#%s', 'User': '%s#%s'}%n",
                event.getGuild().getName(), event.getGuild().getId(),
                event.getChannelLeft().getName(), event.getChannelLeft().getId(),
                event.getMember().getUser().getName(), event.getMember().getUser().getId()
        );

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
        System.out.printf(
                "{'Type': 'Reaction', 'Guild_Name': '%s#%s', 'Chennal_Name': '%s#%s', 'Author': '%s#%s', 'MessageID': '%s', 'Emote': '%s'}%n",
                event.getGuild().getName(), event.getGuild().getId(),
                event.getChannel().getName(), event.getChannel().getId(),
                event.getUser().getName(), event.getUser().getId(),
                event.getMessageId(),
                event.getReactionEmote()
        );

        if (!event.getUser().isBot()) {
            onReactionBindCommand(event);
        }
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        CommandManager.ExcuteReactionCommend(event.getReactionEmote(), event, true);
    }

    @Override
    public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event) {
        CommandManager.ExcuteReactionCommend(event.getReactionEmote(), event, false);
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

    /**
     * 메시지 수신 이벤트
     *
     * @param event
     */
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        //로그 출력
        System.out.printf(
                "{'Type': 'Message#%s', 'Guild_Name': '%s#%s', 'Chennal_Name': '%s#%s', 'Author': '%s#%s', 'Context': '%s'}%n",
                event.getMessage().getId(),
                event.getGuild().getName(), event.getGuild().getId(),
                event.getChannel().getName(), event.getChannel().getId(),
                event.getAuthor().getName(), event.getAuthor().getId(),
                event.getMessage().getContentRaw()
        );
        //System.out.println("[Log] " + event.getGuild().getName() + event.getAuthor().getName().getName() + " : " + event.getMessage().getContentDisplay());

        if (event.getAuthor().isBot()) {
            if (commandQueue.size() >= 1) {
                reactionInterface(event);
            }
        }

        //커맨드 모음에 데이터 인풋
        boolean commandRun = commandInterface(event);
        if (commandRun) {
            System.out.printf("{'Error': 'Unknown Command', 'Context': '%s'}%n", event.getMessage().getContentRaw() );
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
                BotInstance.getInstance(event.getGuild().getId()).musicManager.lastPlayMessage = sendMsg;
                System.out.println("[DiscordBotMain] Update Last message");
                return true;
            }
        }
        return false;
    }

    private void ClearLastMessageReaction(MessageReceivedEvent event){
        Message last = BotInstance.getInstance(event.getGuild().getId()).musicManager.lastPlayMessage;
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
        String msg = "";

        msg = event.getMessage().getContentRaw();

        ProcessorManager.ExcuteMessageProcessor(event, msg);

        if (!msg.startsWith("?")) return false;

        for(String cmd : msg.split("\\n")) {
            cmd = cmd.substring(1);
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
    }

    //미완성
    private void wait_reaction(Message msg, String emote) {
        msg.addReaction(emote).queue();
        System.out.printf("[DiscordBotMain] Set Reaction : %s to %s%n", emote, msg.getId());
    }

    @Override
    public void onPostCommand(GenericMessageEvent genericMessageEvent) {
        try {
            MessageReceivedEvent msgEvent = (MessageReceivedEvent) genericMessageEvent;
            System.out.printf("[DiscordBotMain] CommandQueue Add data: %s%n", msgEvent.getMessage().getContentRaw());
            commandQueue.add(msgEvent.getMessage());
        } catch (Exception e) {

        }

    }

    //#endregion
}
