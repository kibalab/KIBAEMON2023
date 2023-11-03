package com.comduck.chatbot.discord.action.commands;

import com.comduck.chatbot.discord.BotInstance;
import com.comduck.chatbot.discord.action.Command;
import com.comduck.chatbot.discord.action.MessageCommand;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

@MessageCommand(name = {"clear", "clr", "cls"})
public class ClearCommand implements Command {

    @Override
    public void OnCommand(BotInstance instance, GenericEvent e, String msg) {
        GenericMessageEvent genEvent = (GenericMessageEvent) e;

        msg = msg.replaceFirst("clear ", "");
        msg = msg.replaceFirst("cls ", "");
        msg = msg.replaceFirst("clr ", "");
        MessageReceivedEvent msgEvent = (MessageReceivedEvent) e;
        System.out.println(msgEvent.getChannel().getIterableHistory().complete().size());
        int i =0;
        for(Message m : msgEvent.getChannel().getIterableHistory().complete()) {

            //if(((MessageReceivedEvent) event).getAuthor().getId().contains("606475718305775636")) {
            msgEvent.getChannel().deleteMessageById(m.getId()).queue();
            //}
            i++;
            if(Integer.parseInt(msg)+1 == i){
                break;
            }
        }
    }

    @Override
    public void OnPostCommand(BotInstance instance, GenericEvent e) {

    }
}
