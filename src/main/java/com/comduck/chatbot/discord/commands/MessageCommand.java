package com.comduck.chatbot.discord.commands;

public @interface MessageCommand {
    String name() default "test";
    int order() default 0;
}
