package com.comduck.chatbot.discord.audiocore.webutil;

import com.comduck.chatbot.discord.BotInstance;
import com.neovisionaries.i18n.CountryCode;
import net.dv8tion.jda.api.entities.Guild;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.data.tracks.GetTrackRequest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpotifyWebUtil {

    static public Track getTrack(Guild guild, String url) throws IOException, ParseException, SpotifyWebApiException {
        BotInstance instance = BotInstance.getInstance(guild.getId());
        instance.spotifyApi.setAccessToken(instance.spotifyApi.clientCredentials().build().execute().getAccessToken());
        return instance.spotifyApi.getTrack(getTrackIdentifier(url)).market(CountryCode.JP).build().execute();
    }

    static private String getTrackIdentifier(String url) {
        String spotifyRegex = "/track/([a-zA-Z0-9]+)";
        if (url.matches("https://open\\.spotify\\.com/track/.+")) {
            return extractUsingRegex(url, spotifyRegex);
        }
        return "";
    }

    static private String extractUsingRegex(String input, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }

    static public URL getUserImage(Guild guild, Track track) throws IOException, ParseException, SpotifyWebApiException {
        return new URL(BotInstance.getInstance(guild.getId()).spotifyApi.getArtist(track.getId()).build().execute().getImages()[0].getUrl());
    }

    static public URL getTrackArt(Track track) throws MalformedURLException {
        return new URL(track.getAlbum().getImages()[0].getUrl());
    }
}
