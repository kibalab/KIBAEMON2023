package com.comduck.chatbot.discord.action.commands;

import com.comduck.chatbot.discord.BotInstance;
import com.comduck.chatbot.discord.CommandManager;
import com.comduck.chatbot.discord.action.Command;
import com.comduck.chatbot.discord.action.MessageCommand;
import com.comduck.chatbot.discord.action.useraction.PlayAction;
import com.neovisionaries.i18n.CountryCode;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchMusicProvider;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeSearchProvider;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.requests.data.tracks.GetTrackRequest;

import java.awt.*;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

//?test
@MessageCommand(name = {"test"}, order = 0)
public class TestCommand implements Command {

    @Override
    public void OnCommand(BotInstance instance, GenericEvent e, String msg, boolean isAdd) throws IOException, ParseException, SpotifyWebApiException {
        MessageReceivedEvent msgEvent = (MessageReceivedEvent) e;
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("OK", null);
        eb.setColor(new Color(0x244aff));
        eb.addField("Test Embed", "이 Embed 메시지는 다용도 테스트 메시지 입니다.", false);
        HashMap<String, String> customData = new HashMap<>();
        customData.put("trackId", "123");

        var action = new PlayAction().Build(msgEvent.getGuild(), customData);
        System.out.println("[TestCommand.java] Button Action Test");
        System.out.println(action.getLabel());
        System.out.println(action.getStyle());

        msgEvent.getChannel().sendMessageEmbeds(eb.build()).addActionRow(action).queue();
    }

    @Override
    public void OnPostCommand(BotInstance instance, GenericEvent e) {

    }
}
