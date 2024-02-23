package com.comduck.chatbot.discord.action.commands;

import com.comduck.chatbot.discord.BotInstance;
import com.comduck.chatbot.discord.action.Command;
import com.comduck.chatbot.discord.action.MessageCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;

import java.awt.*;

@MessageCommand(name = {"join"})
public class JoinCommand implements Command {

    @Override
    public void OnCommand(BotInstance instance, GenericEvent event, String msg, boolean isAdd) {
        GenericMessageEvent genEvent = (GenericMessageEvent) event;

        String VchID = msg.replaceFirst("join", "").replace(" ", "");
        VoiceChannel Vch = null;

        if (event instanceof MessageReceivedEvent) {

            //1. 파라미터값이 있으면 해당 VoiceChennal을 가져옴
            //2. 없으면 요청한 유저가 있는 VoiceChennal을 가져옴
            if(!VchID.equals("")) {
                Vch = ((MessageReceivedEvent) event).getGuild().getVoiceChannelById(VchID);
            } else {
                Vch = ((MessageReceivedEvent) event).getMember().getVoiceState().getChannel().asVoiceChannel();
            }

            genEvent.getChannel().sendMessage(String.format(
                    "> %s 입장 ``%s``",
                    Vch.getName(),
                    ((MessageReceivedEvent) event).getAuthor().getName()
            )).queue();

        }
        AudioManager audiomng = genEvent.getGuild().getAudioManager();

        // VoiceChennal에 입장, 안되면 오류Embed 출력
        try {
            audiomng.openAudioConnection(Vch);
        } catch (Exception e) {

            EmbedBuilder eb = new EmbedBuilder();
            eb.setColor(new Color(0xFF1A1E));
            eb.addField("오류 Error", String.format(
                    "%s 에 입장할수 없습니다.\n``%s``",
                    Vch.getName(),
                    ((MessageReceivedEvent) event).getAuthor().getName()
            ), false);
            genEvent.getChannel().sendMessageEmbeds(eb.build()).queue();

        }
    }

    @Override
    public void OnPostCommand(BotInstance instance, GenericEvent e) {

    }
}
