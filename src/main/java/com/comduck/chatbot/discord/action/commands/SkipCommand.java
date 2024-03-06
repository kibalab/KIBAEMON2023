package com.comduck.chatbot.discord.action.commands;

import com.comduck.chatbot.discord.BotInstance;
import com.comduck.chatbot.discord.action.Command;
import com.comduck.chatbot.discord.action.MessageCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;

import java.awt.*;

@MessageCommand(name = {"skip", "next"})
public class SkipCommand implements Command {

    @Override
    public void OnCommand(BotInstance instance, GenericEvent e, String msg, boolean isAdd) {

        instance.playerInstance.trackScheduler.playNextTrack(true);

        MessageCreateData resultMsg;
        if(instance.playerInstance.player.getPlayingTrack() == null)
        {
            var eb = new EmbedBuilder();
            eb.setColor(new Color(0xff6624));
            eb.addField("경고 Warning", "대기열이 비어 있습니다.", false);
            var embed = eb.build();
            resultMsg = MessageCreateData.fromEmbeds(embed);
        }
        else
        {
            resultMsg = MessageCreateData.fromContent("> 곡 스킵");
        }

        if (e instanceof MessageReceivedEvent) {
            MessageReceivedEvent msgEvent = (MessageReceivedEvent) e;
            msgEvent.getChannel().sendMessageEmbeds(resultMsg.getEmbeds()).queue();
        } else if (e instanceof GenericMessageReactionEvent) {
            GenericMessageReactionEvent reactionEvent = (GenericMessageReactionEvent) e;
            reactionEvent.getChannel().sendMessageEmbeds(resultMsg.getEmbeds()).queue();
        } else if (e instanceof ButtonInteractionEvent) {
            ButtonInteractionEvent reactionEvent = (ButtonInteractionEvent) e;
            reactionEvent.reply(resultMsg).setEphemeral(true).queue();
        }
    }

    @Override
    public void OnPostCommand(BotInstance instance, GenericEvent e) {

    }
}
