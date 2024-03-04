package com.comduck.chatbot.discord.action;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface UserActionMethod {
    String[] command() default {};
    String[] modalId() default "";
    int order() default 0;
}
