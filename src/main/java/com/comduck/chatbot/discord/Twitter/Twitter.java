package com.comduck.chatbot.discord.Twitter;

import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.dto.endpoints.AdditionalParameters;
import io.github.redouane59.twitter.dto.tweet.TweetList;
import io.github.redouane59.twitter.dto.user.User;
import io.github.redouane59.twitter.signature.TwitterCredentials;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

public class Twitter {


    public static HashMap<String, ArrayList<String>> subsTable = new HashMap<>();

    //public static AccessToken ConsumerKey = new AccessToken("up1SNpeA4lL0JTxGmAzXsHgOc", "jy3OYfqEci0ZublJUdQiXPyQHA3pVzTBr3shIbdcWzuPHIjqdk");
    //public static AccessToken accessToken = new AccessToken("1021315809581252609-EskXrodnSHbaN6ofr7BXToqhSdLdA2", "Z4EbwcJPrMMtuRvSip3YiKFZiQ9QZWjgW7HADEuYsejEk");

    public static TwitterClient Login()
    {
        return new TwitterClient(TwitterCredentials.builder()
                .accessToken("1021315809581252609-EskXrodnSHbaN6ofr7BXToqhSdLdA2")
                .accessTokenSecret("Z4EbwcJPrMMtuRvSip3YiKFZiQ9QZWjgW7HADEuYsejEk")
                .apiKey("up1SNpeA4lL0JTxGmAzXsHgOc")
                .apiSecretKey("jy3OYfqEci0ZublJUdQiXPyQHA3pVzTBr3shIbdcWzuPHIjqdk")
                .build());
    }

    public static void AddObserver(JDA bot, String chennalID, String name)
    {
        TwitterClient twitterClient = Login();

        twitterClient.getUserFromUserId(name).getName();

        User result   = twitterClient.getUserFromUserName(name);

        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(new Color(0x1BC3FF));
        eb.setTitle("Start Observe");
        eb.addField("내용",result.getName(),true);
        eb.setFooter("KIBAEMON 2022", null);
        bot.getTextChannelById(chennalID).sendMessage(eb.build()).queue();


        new Observer(twitterClient, bot, chennalID, name).Start();
    }


}
