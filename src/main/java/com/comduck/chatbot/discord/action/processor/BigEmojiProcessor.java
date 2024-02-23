package com.comduck.chatbot.discord.action.processor;

import com.comduck.chatbot.discord.action.Permission;
import com.comduck.chatbot.discord.action.Processor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;

@Permission(guildId = "1042050645320019988", channelId = "", userId = "") // 연구소
@Permission(guildId = "814906163111526412", channelId = "", userId = "") // 배칠수
@Permission(guildId = "1169720331292905562", channelId = "", userId = "") // Bot Test
@Permission(guildId = "873927976197173249") // 오카부
public class BigEmojiProcessor implements Processor {
    @Override
    public void OnProcess(GenericMessageEvent e, String msg) {
        MessageReceivedEvent event = (MessageReceivedEvent) e;

        if (event.getMessage().getMentions().getCustomEmojis().size() == 1 && event.getMessage().getContentRaw().startsWith("<") && event.getMessage().getContentRaw().endsWith(">") && event.getMessage().getGuild().getIdLong() != 102788723690700800L) { // 542727743909920798L
            String emojiUrl = event.getMessage().getMentions().getCustomEmojis().get(0).getImageUrl();
            User user  = event.getMessage().getAuthor();

            event.getMessage().delete().queue();

            EmbedBuilder eb = new EmbedBuilder();
            eb.setAuthor(user.getName(), user.getAvatarUrl(), user.getAvatarUrl());
            eb.setImage(emojiUrl);
            eb.setColor(new Color(0x244aff));
            event.getChannel().sendMessageEmbeds(eb.build()).queue();
        }
    }
}
