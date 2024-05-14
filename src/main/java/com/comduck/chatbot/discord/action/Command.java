package com.comduck.chatbot.discord.action;

import com.comduck.chatbot.discord.BotInstance;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;

public interface Command extends IAction {

    @MessageCommand
    void OnCommand(BotInstance instance, GenericEvent e, String msg, boolean isAdd) throws SQLException, ClassNotFoundException, IOException, ParseException, SpotifyWebApiException, URISyntaxException;

    void OnPostCommand(BotInstance instance, GenericEvent e);
}
