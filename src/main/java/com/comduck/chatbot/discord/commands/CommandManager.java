package com.comduck.chatbot.discord.commands;

import com.comduck.chatbot.reflection.ClassFinder;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

public class CommandManager {

    static public String CommandPackage = "com.comduck.chatbot.discord.commands";

    static public HashMap<String, Command> commands;

    static public void LoadAllCommands()
    {
        String packageName = "com.comduck.chatbot.discord.commands";
        ClassFinder.findAllClassesUsingClassLoader
        System.out.println();
        List<Class<?>> classesInPackage = ClassFinder.getClassesInPackage(packageName);

        for (Class<?> clazz : classesInPackage) {
            System.out.println("Found class: " + clazz.getName());
        }
    }

    static public void ExcuteMessageCommend(String command, GenericMessageEvent event, String msg)
    {
        commands.get(command).OnCommand(event, msg);
        commands.get(command).OnPostCommand(event);
    }
}
