package com.comduck.chatbot.discord.Twitter;

import io.github.redouane59.twitter.TwitterClient;
import io.github.redouane59.twitter.dto.endpoints.AdditionalParameters;
import io.github.redouane59.twitter.dto.tweet.TweetList;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;

import java.awt.*;
import java.time.LocalDateTime;

public class Observer {

    public static TwitterClient twitterClient;
    public JDA bot;
    public String Channel;
    public String targetID;

    public long Interval = 3000;

    public Thread thread;

    public Observer(TwitterClient twitterClient, JDA bot, String Channel, String targetID, long Interval)
    {
        this.twitterClient = twitterClient;
        this.bot = bot;
        this.Channel = Channel;
        this.targetID = targetID;
        this.Interval = Interval;
    }

    public void Start()
    {
        thread = new Thread(new Runnable() {
            int lastMsgCount = 0;

            @Override
            public void run() {
                CheckNewPost();
                while(true) {
                    if(CheckNewPost())
                    {

                        TweetList result = twitterClient.searchTweets("(from:"+targetID+")",
                                AdditionalParameters.builder().recursiveCall(false).maxResults(10).build());

                        if(result.getData().get(0).getText().startsWith("RT ")) continue;

                        EmbedBuilder eb = new EmbedBuilder();
                        eb.setColor(new Color(0x1BC3FF));
                        eb.setAuthor( result.getData().get(0).getUser().getDisplayedName(), "https://twitter.com/" + result.getData().get(0).getUser().getName() + "/status/" + result.getData().get(0).getId(), result.getData().get(0).getUser().getProfileImageUrl());

                        eb.addField("내용", result.getData().get(0).getText(),true);

                        /*

                        for(int i = 0; i< result.getData().get(0).getMedia().size(); i++)
                        {
                            eb.addField("이미지", result.getData().get(0).getMedia().get(i).getMediaUrl(),true);
                            eb.setImage(result.getData().get(0).getMedia().get(i).getMediaUrl());
                        }
                        //이미지 지원안함
                         */

                        eb.setFooter("KIBAEMON 2022", null);
                        bot.getTextChannelById(Channel).sendMessage(eb.build()).queue();
                    }
                    try {
                        Thread.sleep(Interval);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            public boolean CheckNewPost(){
                boolean b = lastMsgCount != twitterClient.getUserFromUserName(targetID).getTweetCount();
                if(b) lastMsgCount = twitterClient.getUserFromUserName(targetID).getTweetCount();
                return b;
            }
        });

        thread.start();
    }

    public void Stop()
    {
        thread.stop();
    }
}
