package com.comduck.chatbot.discord;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class DiscordBotMain extends ListenerAdapter {

    public static void main(String[] args) throws Exception {
        JDABuilder builder = new JDABuilder(AccountType.BOT);
        String token = "NjA2NDc1NzE4MzA1Nzc1NjM2.XULmjw.vYwYU3M816BsjuW-mXxXauGVVx4";
        builder.setToken(token);

        builder.buildAsync();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        System.out.println("We received a message from" + event.getAuthor().getName() + ": " + event.getMessage().getContentDisplay());
    }
}
