package com.comduck.chatbot.discord;

import com.comduck.chatbot.discord.audiocore.GuildMusicManager;
import com.comduck.chatbot.discord.audiocore.PlayerManager;
import com.comduck.chatbot.discord.audiocore.TrackScheduler;
import com.comduck.chatbot.discord.action.Command;
import com.comduck.chatbot.discord.action.MessageCommand;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import org.reflections.Reflections;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class CommandManager {

    static public String CommandPackage = "com.comduck.chatbot.discord.action.commands";

    static public HashMap<String, Command> commands = new HashMap<>();

    static public HashMap<String, BotInstance> Instances = new HashMap<>();

    static public void CreateInstance(Guild guild) {

        System.out.println("[ Guild : "+guild.getName()+"]");

        PlayerManager manager = PlayerManager.getInstance();
        GuildMusicManager musicManager = manager.getGuildMusicManager(guild);

        AudioPlayer player = musicManager.player;
        TrackScheduler scheduler = musicManager.scheduler;

        Instances.put(guild.getId(), new BotInstance(musicManager, player, scheduler));
    }

    static public void LoadAllCommands() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, ClassNotFoundException, MalformedURLException {
        Reflections reflections = new Reflections(CommandPackage);
        Set<Class<? extends Command>> classes = reflections.getSubTypesOf(Command.class);

        System.out.println("[ Commands ]");
        for (Class<? extends Command> p : classes) {

            MessageCommand a = p.getDeclaredAnnotation(MessageCommand.class);

            if(a == null) continue;

            Command cmdObjecct = p.getConstructor().newInstance();

            for (String cmd: a.name()) {
                AddCommand(cmd, cmdObjecct);
            }
        }
    }

    static public void AddCommand(String cmd, Command command) {
        System.out.println("ADD " + cmd + " | " + command.getClass().getName());
        commands.put(cmd, command);
    }

    static public void ExcuteMessageCommend(String command, GenericMessageEvent event, String msg)
    {
        commands.get(command).OnCommand(Instances.get(event.getGuild().getId()), event, msg);
        commands.get(command).OnPostCommand(Instances.get(event.getGuild().getId()), event);
    }
}
