package com.comduck.chatbot.discord.Twitter;

import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.dto.endpoints.AdditionalParameters;
import io.github.redouane59.twitter.dto.tweet.TweetList;
import io.github.redouane59.twitter.dto.user.User;
import io.github.redouane59.twitter.signature.TwitterCredentials;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Twitter {


    public static HashMap<String, ArrayList<Observer>> observers = new HashMap<String, ArrayList<Observer>>();

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

    public static void AddObserver(JDA bot, String chennalID, String[] ids, long Interval)
    {
        TwitterClient twitterClient = Login();

        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(new Color(0x1BC3FF));
        eb.setAuthor("Start Subscribe", null, "https://cdn.cms-twdigitalassets.com/content/dam/about-twitter/en/brand-toolkit/brand-download-img-1.jpg.twimg.768.jpg");

        if(bot.getTextChannelById(chennalID) == null)
        {
            System.out.println("[Twitter] Cannot found channel : " + chennalID);
            return;
        }

        String AccountList = "";

        for(int i = 0; i < ids.length; i++)
        {
            String id = ids[i];

            twitterClient.getUserFromUserId(id).getName();

            User result   = twitterClient.getUserFromUserName(id);
            AccountList += i + 1 + ". " + result.getDisplayedName() + "\n";

            Observer observer = new Observer(twitterClient, bot, chennalID, id, Interval);
            if(observers.containsKey(chennalID)){
                observers.get(chennalID).add(observer);
            }else{
                ArrayList<Observer> l = new ArrayList<Observer>();
                l.add(observer);
                observers.put(chennalID, l);
            }
            observer.Start();
        }
        eb.addField("Accounts", AccountList, true);
        eb.addField("Interval", Interval + "ms",false);
        eb.setFooter("KIBAEMON 2022", null);

        try {
            bot.getTextChannelById(chennalID).sendMessage(eb.build()).queue();
        }catch (Exception e)
        {
            System.out.println("[Twitter] " + e);
        }


    }

    static boolean isChannel(JDA bot, String id)
    {
        List<TextChannel> chs = bot.getTextChannels();

        for (int i = 0; i < chs.size(); i++)
        {
            if(chs.get(i).getId() == id) return true;
        }

        return false;
    }


}
