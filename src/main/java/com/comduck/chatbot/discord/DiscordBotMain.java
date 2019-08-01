package com.comduck.chatbot.discord;

import com.sun.org.apache.xpath.internal.functions.FuncFalse;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Emote;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageReaction;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import net.dv8tion.jda.core.EmbedBuilder;
import java.awt.Color;

public class DiscordBotMain extends ListenerAdapter {

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
    public void onMessageReceived(MessageReceivedEvent event) {
        //로그 출력
        System.out.println(String.format("{'Guild_ID': '%s', 'Author': '%s', 'Context': '%s'}", event.getGuild().getName(), event.getAuthor().getName(), event.getMessage().getContentRaw() ));
        //System.out.println("[Log] " + event.getGuild().getName() + event.getAuthor().getName() + " : " + event.getMessage().getContentDisplay());

        //커맨드 모음에 데이터 인풋
        boolean commandRun =  CommandInterface(event);
        if(!commandRun)
        {
            //System.out.println( String.format( "{'Error': 'Unknown Command', 'Context': '%s'}", event.getMessage().getContentRaw() ) );
        }
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

        BotCommands(event, msg);
        //명령어가 없을경우 false반환
        return false;
    }

    //명렁어
    private void BotCommands(MessageReceivedEvent event, String msg)
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
    }

    private void wait_reaction(Message msg, String emote)
    {
        msg.addReaction(emote).queue();
    }
}
