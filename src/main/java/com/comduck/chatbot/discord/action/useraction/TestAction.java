package com.comduck.chatbot.discord.action.useraction;

import com.comduck.chatbot.discord.ActionManager;
import com.comduck.chatbot.discord.BotInstance;
import com.comduck.chatbot.discord.action.UserAction;
import com.comduck.chatbot.discord.action.UserActionMethod;
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

@UserActionMethod(command = {"test"}, buttonId = "testButton", modalId = "testModal")
public class TestAction implements UserAction {
    @Override
    public Button Build(Guild guild, Message parent) {
        return Button.of(ButtonStyle.DANGER, "testButton", "테스트");
    }

    @Override
    public boolean OnClick(ButtonInteractionEvent event) {
        TextInput body = TextInput.create("any", "아무거나 입력", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Your concerns go here")
                .setMinLength(5)
                .setMaxLength(1000)
                .build();
        Modal modal = Modal.create("testModal", "테스트 모달")
                .addComponents(ActionRow.of(body))
                .build();
        event.replyModal(modal).queue();
        return false;
    }

    @Override
    public boolean OnApply(ModalInteractionEvent event) {
        event.reply("> 요청중입니다, 잠시만기다려주세요.").queue();
        ActionManager.AttachUserAction("test", event.getMessage());
        return false;
    }

    @Override
    public Button OnChangeStatus(Guild guild, Message parent, Button button) {
        button = button.withStyle(ButtonStyle.SUCCESS);
        button = button.withLabel("반응됨");
        return button;
    }
}
