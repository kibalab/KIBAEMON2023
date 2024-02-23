package com.comduck.chatbot.discord.action.commands;

import com.comduck.chatbot.discord.BotInstance;
import com.comduck.chatbot.discord.action.Command;
import com.comduck.chatbot.discord.action.MessageCommand;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;

@MessageCommand(name = {"pause"})
public class PauseCommand implements Command {

    @Override
    public void OnCommand(BotInstance instance, GenericEvent e, String msg, boolean isAdd) {
        instance.player.setPaused(!instance.player.isPaused());


        if (e instanceof MessageReceivedEvent) {
            MessageReceivedEvent msgEvent = (MessageReceivedEvent) e;
            msgEvent.getChannel().sendMessage(String.format("> 일시정지 ``%s``", ((MessageReceivedEvent) e).getAuthor().getName()));
        } else if (e instanceof GenericMessageReactionEvent) {
            GenericMessageReactionEvent reactionEvent = (GenericMessageReactionEvent) e;
            reactionEvent.getChannel().sendMessage(String.format("> 일시정지 ``%s``", ((GenericMessageReactionEvent) e).getUser().getName()));
        } else if (e instanceof ButtonInteractionEvent) {
            ButtonInteractionEvent reactionEvent = (ButtonInteractionEvent) e;
            reactionEvent.reply(String.format("> 일시정지 ``%s``", ((ButtonInteractionEvent) e).getUser().getName())).setEphemeral(true).queue();
        }
    }

    @Override
    public void OnPostCommand(BotInstance instance, GenericEvent e) {

    }
}
