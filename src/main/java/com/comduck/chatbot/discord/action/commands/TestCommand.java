package com.comduck.chatbot.discord.action.commands;

import com.comduck.chatbot.discord.BotInstance;
import com.comduck.chatbot.discord.CommandManager;
import com.comduck.chatbot.discord.action.Command;
import com.comduck.chatbot.discord.action.MessageCommand;
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

//?test
@MessageCommand(name = {"test"}, order = 0)
public class TestCommand implements Command {

    @Override
    public void OnCommand(BotInstance instance, GenericEvent e, String msg, boolean isAdd) throws IOException, ParseException, SpotifyWebApiException {
        instance.spotifyApi.setAccessToken(instance.spotifyApi.clientCredentials().build().execute().getAccessToken());
        final GetTrackRequest trackRequest = instance.spotifyApi.getTrack("6AMKKgiJVmYQ3OHY1R5y9U").market(CountryCode.JP).build();

        YoutubeSearchProvider musicProvider = new YoutubeSearchProvider();

        musicProvider.loadSearchResult(trackRequest.execute().getName(), x -> {
            try {
                CommandManager.commands.get("play").OnCommand(instance, e, "play " + x.uri, true);
                return null;
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } catch (ParseException ex) {
                throw new RuntimeException(ex);
            } catch (SpotifyWebApiException ex) {
                throw new RuntimeException(ex);
            }
        });

        MessageReceivedEvent msgEvent = (MessageReceivedEvent) e;
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("OK", null);
        eb.setColor(new Color(0x244aff));
        eb.addField("Test Embed", "이 Embed 메시지는 다용도 테스트 메시지 입니다.", false);
        eb.addField("Title", trackRequest.execute().getName(), false);
        msgEvent.getChannel().sendMessage(eb.build()).queue();
    }

    @Override
    public void OnPostCommand(BotInstance instance, GenericEvent e) {

    }
}
