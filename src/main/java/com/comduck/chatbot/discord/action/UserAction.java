package com.comduck.chatbot.discord.action;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.HashMap;

public interface UserAction extends IAction {
    public Button Build(Guild guild, HashMap<String, String> customValues);
    @UserActionMethod
    public boolean OnClick(ButtonInteractionEvent event);
    public boolean OnApply(ModalInteractionEvent event);
    public Button OnUpdate(GenericEvent event, Guild guild, Button button);
}
