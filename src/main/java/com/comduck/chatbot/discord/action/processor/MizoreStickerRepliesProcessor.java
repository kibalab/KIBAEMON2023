package com.comduck.chatbot.discord.action.processor;

import com.comduck.chatbot.discord.action.Permission;
import com.comduck.chatbot.discord.action.Processor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;

@Permission(guildId = "814906163111526412", channelId = "", userId = "365857982652743681") // 배칠수에서 미조레한테만 반응하기
public class MizoreStickerRepliesProcessor implements Processor {
    @Override
    public void OnProcess(GenericMessageEvent e, String msg) {
        MessageReceivedEvent event = (MessageReceivedEvent) e;
        if(event.getMessage().getStickers().size() == 0) {
            return;
        }
        else{

            if(event.getMessage().getStickers().stream().findFirst().get().getId().equals("1237783879054790669")){
                e.getChannel().sendMessage(":kissing_cat::stuffed_flatbread:").queue();
            }
            else if(event.getMessage().getStickers().stream().findFirst().get().getId().equals("1237963291545043024")){
                e.getChannel().sendMessage(":pleading_face::broken_heart::birthday:").queue();
            }
        }

    }
}