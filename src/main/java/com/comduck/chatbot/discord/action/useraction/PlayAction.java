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

@UserActionMethod(command = {"play"}, buttonId = "playButton", modalId = "playModal")
public class PlayAction implements UserAction {
    @Override
    public Button Build(Guild guild, Message parent) {
        BotInstance instance = BotInstance.getInstance(guild.getId());
        AudioPlayer player = instance.playerInstance.player;

        String label;
        ButtonStyle style;


        if(instance.playerInstance.trackScheduler.playing == null) {
            // 재생중인 곡이 없을때
            if(instance.playerInstance.trackScheduler.isEmpty()) {
                style = ButtonStyle.PRIMARY;
                label = "곡추가";
            }
            else {
                style = ButtonStyle.UNKNOWN; // 엑션 제거
                label = "오류";
            }
        }
        else{
            //재생중인 곡이 있을때
            if (instance.playerInstance.trackScheduler.playing.Message.equals(parent)) {
                if(player.isPaused()) {
                    style = ButtonStyle.DANGER;
                    label = "일시정지";
                }
                else {
                    style = ButtonStyle.SUCCESS;
                    label = "재생중";
                }
            }
            else{
                style = ButtonStyle.UNKNOWN; // 엑션 제거
                label = "오류";
            }
        }

        return style == ButtonStyle.UNKNOWN ? null : Button.of(style, "playButton", label);
    }

    @Override
    public boolean OnClick(ButtonInteractionEvent event) {
        TextInput body = TextInput.create("url", "Video URL", TextInputStyle.PARAGRAPH)
            .setPlaceholder("Your concerns go here")
            .setMinLength(5)
            .setMaxLength(1000)
            .build();
        Modal modal = Modal.create("playModal", "영상 재생하기")
            .addComponents(ActionRow.of(body))
            .build();
        event.replyModal(modal).queue();
        return false;
    }

    @Override
    public boolean OnApply(ModalInteractionEvent event) {
        ActionManager.ExcuteMessageCommend("play", new MessageReceivedEvent(event.getJDA(), -1, event.getMessage()), event.getValue("url").getAsString(), true);
        event.reply("> 요청중입니다, 잠시만기다려주세요.").setEphemeral(true).queue();
        return false;
    }

    @Override
    public Button OnChangeStatus(Guild guild, Message parent, Button button) {
        BotInstance instance = BotInstance.getInstance(guild.getId());
        AudioPlayer player = instance.playerInstance.player;

        String label;
        ButtonStyle style;

        if(player.isPaused()) {
            style = ButtonStyle.DANGER;
            label = "일시정지";
        } else if(instance.playerInstance.trackScheduler.playing.Message == null) {
            style = ButtonStyle.PRIMARY;
            label = "곡추가";
        } else if (instance.playerInstance.trackScheduler.playing.Message.equals(parent)) {
            style = ButtonStyle.SUCCESS;
            label = "재생중";
        } else {
            style = ButtonStyle.UNKNOWN;
            label = "오류";
        }
        System.out.println("[PlayAction] Build : " + label);

        return style == ButtonStyle.UNKNOWN ? null : Button.of(style, "playButton", label);
    }
}
