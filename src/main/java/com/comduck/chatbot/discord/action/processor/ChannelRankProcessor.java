package com.comduck.chatbot.discord.action.processor;

import com.comduck.chatbot.discord.BotInstance;
import com.comduck.chatbot.discord.DiscordBotMain;
import com.comduck.chatbot.discord.action.Permission;
import com.comduck.chatbot.discord.action.Processor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Permission(guildId = "814906163111526412", channelId = "", userId = "") // 배칠수
public class ChannelRankProcessor implements Processor {

    static Timer timer = null;
    static final String[] numbers = new String[] {":one:", ":two:", ":three:", ":four:", ":five:", ":six:", ":seven:", ":eight:", ":nine:", ":ten:"};

    @Override
    public void OnProcess(GenericMessageEvent e, String msg) {
        if(e.getChannel().getId() == "1230112474389549137") return;

        if(timer == null) {
            timer = new Timer();

            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    var instance = BotInstance.getInstance("814906163111526412"); // 배칠수
                    Map<String, List<Message>> bychannel = instance.lastHourMessage.stream().collect(Collectors.groupingBy(Message::getChannelId));

                    for (Map.Entry<String, List<Message>> entry : bychannel.entrySet()) {
                        if(instance.lastRank.containsKey(entry.getKey()))
                            instance.lastRank.put(entry.getKey(), entry.getValue().size() - instance.lastRank.get(entry.getKey()));
                        else instance.lastRank.put(entry.getKey(), entry.getValue().size());
                    }

                    List<Map.Entry<String, Integer>> entryList = new LinkedList<>(instance.lastRank.entrySet());
                    entryList.sort(Map.Entry.comparingByValue());
                    Collections.reverse(entryList);

                    StringBuilder text = new StringBuilder("# [실시간 북적 갤러리 순위]\n");
                    var count = 0;
                    for (Map.Entry<String, Integer> ele : entryList)
                    {
                        var countDelta = ele.getValue().intValue();

                        text.append(String.format("%s <#%s> : %d개 | ", numbers[count++], ele.getKey(),bychannel.get(ele.getKey()).size()));
                        text.append(String.format("%d%s |", countDelta, (countDelta > 0 ? ":small_red_triangle:" : (countDelta == 0 ? ":heavy_minus_sign:" : ":small_red_triangle_down:"))));
                        text.append("\n");

                    if(count >= 9) break;
                    }
                    text.append(String.format("마지막 업데이트 : %s", OffsetDateTime.now()));
                    text.append("\n");

                    for (Map.Entry<String, List<Message>> entry : bychannel.entrySet()) {
                        instance.lastRank.put(entry.getKey(), entry.getValue().size());
                    }

                    var channel = DiscordBotMain.jda.getGuildById("814906163111526412").getChannelById(TextChannel.class, "1230112474389549137");
                    if(instance.lastRankMsg == null) channel.sendMessage(text.toString()).queue(msg -> instance.lastRankMsg = msg);
                    else instance.lastRankMsg.editMessage(text.toString()).queue(msg -> instance.lastRankMsg = msg);
                }
            };

            timer.scheduleAtFixedRate(task, 1000, 300000);
        }


        var ev = (MessageReceivedEvent) e;
        var instance = BotInstance.getInstance(ev.getGuild().getId());
        instance.lastHourMessage.offer(ev.getMessage());

        if(instance.lastHourMessage.isEmpty()) return;
        var peek = instance.lastHourMessage.peek();
        var created = peek.getTimeCreated();
        var now = OffsetDateTime.now().toInstant();
        var h = Duration.between(now, created.toInstant()).toHoursPart();
        if(h > 1) instance.lastHourMessage.poll();
    }
}
