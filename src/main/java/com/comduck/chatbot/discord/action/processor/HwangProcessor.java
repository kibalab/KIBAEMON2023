package com.comduck.chatbot.discord.action.processor;

import com.comduck.chatbot.discord.action.Permission;
import com.comduck.chatbot.discord.action.Processor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Permission(guildId = "814906163111526412", channelId = "", userId = "") // 배칠수
@Permission(guildId = "873927976197173249") // 오카부
public class HwangProcessor implements Processor {
    @Override
    public void OnProcess(GenericMessageEvent e, String msg) {
        MessageReceivedEvent event = (MessageReceivedEvent) e;

        var processed_msg = event.getMessage().getContentRaw().replace(" ", "");

        if(!event.getAuthor().isSystem() && !event.getAuthor().isBot())
        {
            Pattern pattern = Pattern.compile("흐(어+)엉");
            Matcher matcher = pattern.matcher(event.getMessage().getContentRaw());

            if (matcher.find()) {
                String matchedWord = matcher.group();
                event.getChannel().sendMessage(matchedWord.replace("흐어", "흐어어어어어")).queue();
            }

            pattern = Pattern.compile("(\\s*([^가-후]|^)(황|(스([A-Z|0-9|@$!%*#?&]*)코)|스([A-Z|0-9|@$!%*#?&]*)페([A-Z|0-9|@$!%*#?&]*)이([A-Z|0-9|@$!%*#?&]*)스([A-Z|0-9|@$!%*#?&]*)코([A-Z|0-9|@$!%*#?&]*)어|황([A-Z|0-9|@$!%*#?&]*)지([A-Z|0-9|@$!%*#?&]*)민|코([A-Z|0-9|@$!%*#?&]*)트([A-Z|0-9|@$!%*#?&]*)스([A-Z|0-9|@$!%*#?&]*)코)([은|는|이|가|도|과])?\\s*([^가-후]))|^(황|(스([A-Z|0-9|@$!%*#?&]*)코)|스([A-Z|0-9|@$!%*#?&]*)페([A-Z|0-9|@$!%*#?&]*)이([A-Z|0-9|@$!%*#?&]*)스([@$!%*#?&]?[A-Z]?[0-9]?)코([A-Z|0-9|@$!%*#?&]*)어|황([A-Z|0-9|@$!%*#?&]*)지([A-Z|0-9|@$!%*#?&]*)민|코([A-Z|0-9|@$!%*#?&]*)트([A-Z|0-9|@$!%*#?&]*)스([A-Z|0-9|@$!%*#?&]*)코)([은|는|이|가|도|과])?$");
            matcher = pattern.matcher(event.getMessage().getContentRaw());
            if (matcher.find()) {
                event.getChannel().sendMessage("흐어어어어엉").queue();
            }
        }
    }
}
