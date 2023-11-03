package com.comduck.chatbot.discord.action.processor;

import com.comduck.chatbot.discord.action.Permission;
import com.comduck.chatbot.discord.action.Processor;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Permission(guildId = "814906163111526412") // 배칠수
@Permission(guildId = "1169720331292905562") // Bot Test
public class TwitterProcessor implements Processor {

    @Override
    public void OnProcess(GenericMessageEvent e, String msg) {
        Pattern pattern = Pattern.compile("(https?://)(www\\.)?(twitter.com|x.com)(/\\S*)\\b");
        Matcher matcher = pattern.matcher(msg);

        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String replacedString = matcher.group(1) + "fxtwitter.com" + matcher.group(4);
            e.getChannel().sendMessage(replacedString).queue();
        }
    }
}
