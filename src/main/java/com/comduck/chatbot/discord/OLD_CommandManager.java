package com.comduck.chatbot.discord;

import net.dv8tion.jda.api.events.message.GenericMessageEvent;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OLD_CommandManager {

    public void twitterCommand(GenericMessageEvent event, String msg)
    {
        if(event.getGuild().getIdLong() == 814906163111526412L || event.getGuild().getIdLong() == 1139237196902178881L)
        {
            Pattern pattern = Pattern.compile("(https?://)(www\\.)?(twitter.com|x.com)(/\\S*)\\b");
            Matcher matcher = pattern.matcher(msg);

            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String replacedString = matcher.group(1) + "fxtwitter.com" + matcher.group(4);
                event.getChannel().sendMessage(replacedString).queue();
            }
        }
    }
}
