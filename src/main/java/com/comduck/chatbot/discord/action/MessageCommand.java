package com.comduck.chatbot.discord.action;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface MessageCommand {
    String[] name() default {"test"};
    int order() default 0;
}
