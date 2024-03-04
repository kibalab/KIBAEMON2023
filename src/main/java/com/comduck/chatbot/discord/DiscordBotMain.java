package com.comduck.chatbot.discord;

import com.comduck.chatbot.discord.audioV2.QuickController;
import com.comduck.chatbot.discord.audiocore.*;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.entities.sticker.StickerItem;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import se.michaelthelin.spotify.SpotifyApi;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.*;

public class DiscordBotMain extends ListenerAdapter {
    public static SpotifyApi spotifyApi = new SpotifyApi.Builder().setClientId("ee42dee9338d44b5a1dba476c5e75055").setClientSecret("da310a9627714fdfa2869742ed022986").build();
    public static boolean logging = false;


    public static void main(String[] args) throws Exception {
        StartArgumentCommand(args);
    }

    public static void StartArgumentCommand(String[] Arg) throws Exception {

        for(int i = 0; Arg.length > i; i++)
        {
            switch (Arg[i])
            {
                case "--run" :
                case "-r" :
                    new DiscordBotMain().start(Arg[++i]);
                    System.out.println("[DiscordBotMain] Set Bot Client : " + Arg[i]);
                    break;

                case "-b" :
                case "--bots" :
                    JSONObject bots = (JSONObject) (new JSONParser().parse(new FileReader("Bots.json")));
                    Iterator<String> keys = (Iterator<String>) bots.keySet();
                    System.out.println("[ Bots.js List ]");
                    int index = 1;
                    while(keys.hasNext()) {
                        String key = keys.next();
                        System.out.printf("%d. %s : %s%n", index, key, bots.get(key));
                        index++;
                    }
                    break;

                case "-l" :
                case "--log" :
                    logging = Boolean.parseBoolean(Arg[++i].toLowerCase());
                    System.out.println("[DiscordBotMain] Set Logger Activation : " + logging);
                    break;

                default :
                    System.out.println("[ KIBATION2019-JAVA ]");
                    System.out.println("[ K13A_Laboratories ]\n");
                    System.out.println("-r  --run   {BotName}   : Login with Token in Bots.json.");
                    System.out.println("-h  --help      : Print out help.");
                    System.out.println("-b  --bot      : Output the bot list in Bots.json.");
                    System.out.println("-l  --log   {true|false}   : Set Message Logger activation");
                    break;
            }
        }
    }

    private void start(String bot) throws Exception {

        ResourceManager.loadAll();
        CommandManager.LoadAllCommands();
        ProcessorManager.LoadAllCommands();

        ImageIO.scanForPlugins();

        JSONObject bots = (JSONObject) (new JSONParser().parse(new FileReader("Bots.json")));

        String token = (String)bots.get(bot);

        JDABuilder builder = JDABuilder.createDefault(token);
        builder.setToken(token);
        builder.enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_EMOJIS_AND_STICKERS, GatewayIntent.GUILD_MEMBERS);
        builder.setActivity(Activity.playing("<가동중> ?help"));
        builder.addEventListeners(this);
        builder.build();
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        super.onGuildJoin(event);
        new BotInstance(event.getGuild(), spotifyApi);
    }

    @Override
    public void onGuildReady(GuildReadyEvent event) {
        if (logging) {
            String str = null;
            str = event.getGuild().getName() + "[" + event.getGuild().getId() + "]" + " : ";
            for (TextChannel c : event.getGuild().getTextChannels()) {
                str += c.getName() + "[" + c.getId() + "]" + " : " + c.canTalk() + " : {";
                for (Member m : c.getMembers()) {
                    str += m.getNickname() + "(" + m.getUser().getName() + "#" + m.getUser().getId() + ")";
                }
                str += "}\n";
            }
            System.out.println(str);
        }

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
                channel.sendMessageEmbeds(eb.build()).queue();
            }
        }

    }

    @Override
    public void onShutdown(ShutdownEvent event) {
        super.onShutdown(event);
    }

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        AudioChannelUnion joinedChannel = event.getChannelJoined();
        AudioChannelUnion leftChannel = event.getChannelLeft();

        String state = "";
        VoiceChannel channel = null;

        if (joinedChannel != null) {
            state = "JoinVoice";
            channel = event.getChannelJoined().asVoiceChannel();
        }
        if (leftChannel != null) {
            state = "LeaveVoice";
            channel = event.getChannelLeft().asVoiceChannel();
        }
        if (joinedChannel != null && leftChannel != null) {
            state = "MoveVoice";
        }

        if (logging) System.out.printf(
                "{'Type': '%s', 'Guild_Name': '%s#%s', 'VoiceChennal_Name': '%s#%s', 'User': '%s#%s'}%n",
                state,
                event.getGuild().getName(), event.getGuild().getId(),
                channel.getName(), channel.getId(),
                event.getMember().getUser().getName(), event.getMember().getUser().getId()
        );
    }

    /**
     * 리액션 수신 이벤트
     *
     * @param event
     */

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (event.getUser().isBot()) return;
        if (logging) System.out.printf(
                "{'Type': 'ReactionAdd', 'Guild_Name': '%s#%s', 'Chennal_Name': '%s#%s', 'Author': '%s#%s', 'MessageID': '%s', 'Emote': '%s'}%n",
                event.getGuild().getName(), event.getGuild().getId(),
                event.getChannel().getName(), event.getChannel().getId(),
                event.getUser().getName(), event.getUser().getId(),
                event.getMessageId(),
                event.getReaction().getEmoji().getAsReactionCode()
        );
        CommandManager.ExcuteReactionCommend(event.getReaction(), event, true);
    }

    @Override
    public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event) {
        if (event.getUser().isBot()) return;
        if (logging) System.out.printf(
                "{'Type': 'ReactionRemove', 'Guild_Name': '%s#%s', 'Chennal_Name': '%s#%s', 'Author': '%s#%s', 'MessageID': '%s', 'Emote': '%s'}%n",
                event.getGuild().getName(), event.getGuild().getId(),
                event.getChannel().getName(), event.getChannel().getId(),
                event.getUser().getName(), event.getUser().getId(),
                event.getMessageId(),
                event.getReaction().getEmoji().getAsReactionCode()
        );
        CommandManager.ExcuteReactionCommend(event.getReaction(), event, false);
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        CommandManager.ExcuteButtonAction(event.getButton().getId().split(" ")[0], event, "");
    }

    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        CommandManager.ExcuteModalAction(event.getModalId().split(" ")[0], event, "");
    }

    /**
     * 메시지 수신 이벤트
     *
     * @param event
     */
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        //로그 출력
        if (logging) System.out.printf(
                "{'Type': 'Message#%s', 'Guild_Name': '%s#%s', 'Chennal_Name': '%s#%s', 'Author': '%s#%s', 'Context': '%s'}%n",
                event.getMessage().getId(),
                event.getGuild().getName(), event.getGuild().getId(),
                event.getChannel().getName(), event.getChannel().getId(),
                event.getAuthor().getName(), event.getAuthor().getId(),
                event.getMessage().getContentRaw()
        );

        List<StickerItem> stickers = event.getMessage().getStickers();
        if (logging) stickers.forEach(stickerItem -> {
            System.out.printf(
                    "{'Type': 'Sticker#%s', 'Guild_Name': '%s#%s', 'Chennal_Name': '%s#%s', 'Author': '%s#%s', 'Context': '%s'}%n",
                    event.getMessage().getId(),
                    event.getGuild().getName(), event.getGuild().getId(),
                    event.getChannel().getName(), event.getChannel().getId(),
                    event.getAuthor().getName(), event.getAuthor().getId(),
                    stickerItem.getName()+ "#" + stickerItem.getId()
            );
        });

        //커맨드 모음에 데이터 인풋
        boolean commandRun = commandInterface(event);
        if (commandRun) {
            System.out.printf("{'Error': 'Unknown Command', 'Context': '%s'}%n", event.getMessage().getContentRaw() );
        }
    }

    private void ClearLastMessageReaction(MessageReceivedEvent event){
        Message last = BotInstance.getInstance(event.getGuild().getId()).playerInstance.lastPlayMessage;
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

    //#endregion
}
