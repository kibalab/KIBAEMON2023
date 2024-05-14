package com.comduck.chatbot.discord;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.utils.data.DataObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.Arrays;

public class CommandEvent<T extends GenericEvent> {
    public static String prefix = "?";
    public final String name;
    public final String[] args;
    public final T event;

    public CommandEvent(T event, String message)
    {
        var tmp = message.split(" ");
        this.name = tmp[0].replace(prefix, "");
        this.args = Arrays.copyOfRange(tmp, 0, tmp.length - 1);
        this.event = event;
    }

    @Override
    public String toString() {
        return String.format("<CommandEvent>(prfx: \"%s\", cmd: \"%s\", args: [%s])", prefix, name, String.join(", ", args));
    }
}
