package com.comduck.chatbot.discord.action;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Repeatable(Permissions.class)
@Retention(RetentionPolicy.RUNTIME)
public @interface Permission {
    String guildId() default "";
    String channelId() default "";
    String userId() default "";
}

