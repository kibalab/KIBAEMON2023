package com.comduck.chatbot.discord.action.commands;

import com.comduck.chatbot.discord.BotInstance;
import com.comduck.chatbot.discord.action.Category;
import com.comduck.chatbot.discord.action.Command;
import com.comduck.chatbot.discord.action.MessageCommand;
import com.comduck.chatbot.discord.action.commands.util.TimeUtil;
import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.model.YoutubeVideo;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@MessageCommand(name = {"track", "tlist", "tl", "slist", "queue", "q"}, desc = "현재 대기열 목록을 출력합니다.", cat = Category.Audio)
public class TracklistCommand implements Command {

    @Override
    public void OnCommand(BotInstance instance, GenericEvent event, String msg, boolean isAdd) {

        String channelID = "";
        MessageReceivedEvent msgEvent = null;
        GenericMessageReactionEvent reactionEvent = null;
        ButtonInteractionEvent buttonEvent = null;

        if (event instanceof MessageReceivedEvent) {
            msgEvent = (MessageReceivedEvent) event;
            channelID = msgEvent.getChannel().getId();
        } else if (event instanceof GenericMessageReactionEvent) {
            reactionEvent = (GenericMessageReactionEvent) event;
            channelID = reactionEvent.getChannel().getId();
        } else if (event instanceof ButtonInteractionEvent) {
            buttonEvent = (ButtonInteractionEvent) event;
            channelID = buttonEvent.getChannelId();
        }

        //재생되고 있는 트랙이 있는지 확인
        // (재생되고 있는 트랙이 없으면 tracklist도 비어있는걸로 판단)
        if (instance.playerInstance.player.getPlayingTrack() == null) {
            EmbedBuilder eb = new EmbedBuilder();
            eb.setColor(new Color(0xff6624));
            if (event instanceof MessageReceivedEvent) {
                channelID = msgEvent.getChannel().getId();
                eb.addField("경고 Warning", String.format(
                        "대기열이 비어 있습니다.\n``%s``",
                        ((MessageReceivedEvent) event).getAuthor().getName()
                ), false);
                msgEvent.getChannel().sendMessageEmbeds(eb.build()).queue();
            } else if (event instanceof GenericMessageReactionEvent) {
                channelID = reactionEvent.getChannel().getId();
                eb.addField("경고 Warning", String.format(
                        "대기열이 비어 있습니다.\n``%s``",
                        ((GenericMessageReactionEvent) event).getUser().getName()
                ), false);
                reactionEvent.getChannel().sendMessageEmbeds(eb.build()).queue();
            } else if (event instanceof ButtonInteractionEvent) {
                channelID = buttonEvent.getChannelId();
                buttonEvent.reply("대기열이 비어 있습니다.").setEphemeral(true).queue();
            }
        } else { // 비어있지 않으면

            //현재재생되고 있는 트랙의 정보를 가져옴 (title, Position, Duration)
            AudioTrackInfo info = instance.playerInstance.player.getPlayingTrack().getInfo();

            EmbedBuilder eb = new EmbedBuilder();
            eb.setColor(new Color(0x244aff));
            eb.setTitle(String.format(
                    "재생중 - %s [%s/%s] \n",
                    info.title,
                    TimeUtil.formatTime(instance.playerInstance.player.getPlayingTrack().getPosition()),
                    TimeUtil.formatTime(instance.playerInstance.player.getPlayingTrack().getDuration())
            ), instance.playerInstance.player.getPlayingTrack().getInfo().uri);

            //1. tracklist 큐의 데이터를 리스트로 복제
            //2. list의 인덱스를 확인하여 문자열에 담음
            //3. Embed메시지를 생성하여 출력
            List playelist = new ArrayList(instance.playerInstance.trackScheduler.trackCount());
            String str = "";
            long TotalDuration = instance.playerInstance.player.getPlayingTrack().getDuration() - instance.playerInstance.player.getPlayingTrack().getPosition();
            boolean overtext = false;
            if (playelist.size() != 0) {
                for (int i = 0; true; i++) {
                    if (playelist.size() == i) {
                        break;
                    }
                    AudioTrack t = (AudioTrack) playelist.get(i);
                    if(str.length() >= 900 && !overtext){
                        str += String.format(" ``[ +%d곡 ]`` ", playelist.size()-i);
                        overtext = true;
                        System.out.println(str.length());
                    }
                    String title = t.getInfo().title;
                    if(title.length() > 65) {
                        title = t.getInfo().title.substring(0, 65) + "...";
                    }
                    if(!overtext) {
                        str += String.format("%d. %s ``[%s]``\n", i + 1, title, TimeUtil.formatTime(t.getInfo().length));
                    }
                    TotalDuration += t.getInfo().length;
                }
            } else {
                str = "None";
            }

            if(buttonEvent != null) {
                buttonEvent.reply(String.format(
                        "재생중 - %s [%s/%s] \n%s",
                        info.title,
                        TimeUtil.formatTime(instance.playerInstance.player.getPlayingTrack().getPosition()),
                        TimeUtil.formatTime(instance.playerInstance.player.getPlayingTrack().getDuration()),
                        str
                )).setEphemeral(true).queue();
                return;
            }

            try {
                String tsplist = "";
                YoutubeDownloader yd = new YoutubeDownloader();
                YoutubeVideo video = yd.getVideo(instance.playerInstance.player.getPlayingTrack().getIdentifier());
                String dsc = video.details().description();
                System.out.println(dsc);
                Pattern p1 = Pattern.compile("([0-9]{2}):([0-9]{2})((\\s*)?)(([\\S|\\s]*))(\\n?)");
                Pattern p2 = Pattern.compile("([0-9]{2}):([0-9]{2}):([0-9]{2})((\\s*)?)(([\\S|\\s]*))(\\n?)");
                Matcher m1 = p1.matcher(dsc); Matcher m2 = p2.matcher(dsc);
                while (m1.find()) {
                    tsplist += m1.group() + "\n";
                }
                eb.addField("TimeStemp", tsplist, false);

            }catch (Exception e) {
                e.printStackTrace();
            }

            eb.addField("TrackList", str, false);
            eb.setFooter(String.format("TotalDuration - %s", TimeUtil.formatTime(TotalDuration)), null);
            event.getJDA().getTextChannelById(channelID).sendMessageEmbeds(eb.build()).queue();
        }
    }

    @Override
    public void OnPostCommand(BotInstance instance, GenericEvent e) {

    }
}
