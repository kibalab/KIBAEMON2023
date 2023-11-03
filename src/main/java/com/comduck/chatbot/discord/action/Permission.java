package com.comduck.chatbot.discord.action;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Repeatable(Permissions.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface Permission {
    public String guildId() default "";
    public String channelId() default "";
    public String userId() default "";
}

