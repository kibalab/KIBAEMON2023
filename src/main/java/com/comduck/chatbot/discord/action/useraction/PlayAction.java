package com.comduck.chatbot.discord.action.useraction;

import com.comduck.chatbot.discord.BotInstance;
import com.comduck.chatbot.discord.ActionManager;
import com.comduck.chatbot.discord.action.UserAction;
import com.comduck.chatbot.discord.action.UserActionMethod;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.entities.Guild;
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
    public Button Build(Guild guild, HashMap<String, String> customValues) {
        BotInstance instance = BotInstance.getInstance(guild.getId());
        AudioPlayer player = instance.playerInstance.player;

        ButtonStyle style = ButtonStyle.UNKNOWN;
        String msg = "알수없음";

        if(player.isPaused())
        {
            style = ButtonStyle.DANGER;
            msg = "일시정지";
        } else if (customValues.get("trackId").contains(player.getPlayingTrack().getIdentifier())) {
            style = ButtonStyle.SUCCESS;
            msg = "재생중";
        } else {
            style = ButtonStyle.PRIMARY;
            msg = "곡 추가";
        }
        return Button.of(style, "playButton", msg);
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
        event.reply("> 요청중입니다, 잠시만기다려주세요.").queue();
        return false;
    }

    @Override
    public Button OnChangeStatus(Guild guild, Button button) {
        BotInstance instance = BotInstance.getInstance(guild.getId());
        AudioPlayer player = instance.playerInstance.player;
        
        ButtonStyle style = ButtonStyle.UNKNOWN;
        String msg = "알수없음";

        if(player.isPaused())
        {
            style = ButtonStyle.DANGER;
            msg = "일시정지";
        } else if (button.getId().contains(player.getPlayingTrack().getIdentifier())) {
            style = ButtonStyle.SUCCESS;
            msg = "재생중";
        } else {
            style = ButtonStyle.PRIMARY;
            msg = "곡 추가";
        }

        return button.withStyle(style).withLabel(msg);
    }
}
