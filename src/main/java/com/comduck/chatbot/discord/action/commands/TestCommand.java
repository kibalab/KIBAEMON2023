package com.comduck.chatbot.discord.action.commands;

import com.comduck.chatbot.discord.BotInstance;
import com.comduck.chatbot.discord.action.Command;
import com.comduck.chatbot.discord.action.MessageCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;

//?test
@MessageCommand(name = {"test"}, order = 0)
public class TestCommand implements Command {

    @Override
    public void OnCommand(BotInstance instance, GenericEvent e, String msg) {
        MessageReceivedEvent msgEvent = (MessageReceivedEvent) e;
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("OK", null);
        eb.setColor(new Color(0x244aff));
        eb.addField("Test Embed", "이 Embed 메시지는 다용도 테스트 메시지 입니다.", false);
        msgEvent.getChannel().sendMessage(eb.build()).queue();
    }

    @Override
    public void OnPostCommand(BotInstance instance, GenericEvent e) {

    }
}
