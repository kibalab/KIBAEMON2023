package com.comduck.chatbot.discord.action.useraction;

import com.comduck.chatbot.discord.BotInstance;
import com.comduck.chatbot.discord.ActionManager;
import com.comduck.chatbot.discord.action.UserAction;
import com.comduck.chatbot.discord.action.UserActionMethod;
import com.comduck.chatbot.discord.audioV2.TrackScheduler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

import java.util.HashMap;

@UserActionMethod(command = {"skip", "play"}, buttonId = "skipButton", modalId = "skipModal")
public class SkipAction implements UserAction {
    @Override
    public Button Build(Guild guild, Message parent) {
        BotInstance instance = BotInstance.getInstance(guild.getId());
        AudioPlayer player = instance.playerInstance.player;

        String label;
        ButtonStyle style;

        style = ButtonStyle.SECONDARY;
        label = "스킵";

        var deleteOption = instance.playerInstance.trackScheduler.playing == null;

        if (deleteOption || !instance.playerInstance.trackScheduler.playing.Message.equals(parent))
        {
            style = ButtonStyle.UNKNOWN;
            label = "오류";
        }

        return style == ButtonStyle.UNKNOWN ? null : Button.of(style, "skipButton", label);
    }

    @Override
    public boolean OnClick(ButtonInteractionEvent event) {
        ActionManager.ExcuteMessageCommend(new MessageReceivedEvent(event.getJDA(), -1, event.getMessage()), "?skip", true);
        event.reply("> 요청중입니다, 잠시만기다려주세요.").setEphemeral(true).queue();
        return false;
    }

    @Override
    public boolean OnApply(ModalInteractionEvent event) {
        event.reply("> 요청중입니다, 잠시만기다려주세요.").setEphemeral(true).queue();
        return false;
    }

    @Override
    public Button OnChangeStatus(Guild guild, Message parent, Button button) {
        return button;
    }
}
