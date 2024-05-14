package com.comduck.chatbot.discord.action.processor;

import com.comduck.chatbot.discord.action.Permission;
import com.comduck.chatbot.discord.action.Processor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;

@Permission(guildId = "814906163111526412", channelId = "", userId = "365857982652743681") // 배칠수에서 미조레한테만 반응하기
// Bot Test
//@Permission(guildId = "1169720331292905562", channelId = "", userId = "341178729927671814 ")
public class MizoreStickerRepliesProcessor implements Processor {
    @Override
    public void OnProcess(GenericMessageEvent e, String msg) {
        MessageReceivedEvent event = (MessageReceivedEvent) e;
        if(event.getMessage().getStickers().size() == 0) {
            return;
        }
        else{

            if(event.getMessage().getStickers().stream().findFirst().get().getId().equals("1237783879054790669")){
                //이렘 빵콘
                event.getMessage().addReaction(Emoji.fromUnicode("U+1F63D")).queue();
                event.getMessage().addReaction(Emoji.fromUnicode("U+1F35E")).queue();
            }
            else if(event.getMessage().getStickers().stream().findFirst().get().getId().equals("1237963291545043024")){
                //키이나 쓸쓸한 파티
                event.getMessage().addReaction(Emoji.fromUnicode("U+1F97A")).queue();
                event.getMessage().addReaction(Emoji.fromUnicode("U+1F494")).queue();
                event.getMessage().addReaction(Emoji.fromUnicode("U+1F382")).queue();
            }
            else if(event.getMessage().getStickers().stream().findFirst().get().getId().equals("1237784110055952424")){
                //이렘 울먹
                event.getMessage().addReaction(Emoji.fromUnicode("U+1F63F")).queue();
            }
            else if(event.getMessage().getStickers().stream().findFirst().get().getId().equals("1237782989606228160")){
                //이렘 (화들짝)
                event.getMessage().addReaction(Emoji.fromUnicode("U+1F640")).queue();
            }
            else if(event.getMessage().getStickers().stream().findFirst().get().getId().equals("1237782813819015269")){
                //나쟈 울먹
                event.getMessage().addReaction(Emoji.fromUnicode("U+1F97A")).queue();
                event.getMessage().addReaction(Emoji.fromUnicode("U+1F64F")).queue();
            }
        }

    }
}