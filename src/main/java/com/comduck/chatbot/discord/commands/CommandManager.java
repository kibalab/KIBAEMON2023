package com.comduck.chatbot.discord.commands;

import com.comduck.chatbot.discord.BotInstance;
import com.comduck.chatbot.discord.audiocore.GuildMusicManager;
import com.comduck.chatbot.discord.audiocore.PlayerManager;
import com.comduck.chatbot.discord.audiocore.PostCommandListener;
import com.comduck.chatbot.discord.audiocore.TrackScheduler;
import com.comduck.chatbot.reflection.ClassFinder;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class CommandManager {

    static public String CommandPackage = "com.comduck.chatbot.discord.commands";

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

    static public void LoadAllCommands() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Reflections reflections = new Reflections(CommandPackage);
        Set<Class<? extends Command>> classes = reflections.getSubTypesOf(Command.class);

        System.out.println("[ Commands ]");
        for (Class<? extends Command> p : classes) {

            MessageCommand a = p.getDeclaredAnnotation(MessageCommand.class);

            if(a == null) continue;

            AddCommand(a.name(), p.getConstructor().newInstance());
        }
    }

    static public void AddCommand(String cmd, Command command) {
        System.out.println(cmd + " | " + command.getClass().getName());
        commands.put(cmd, command);
    }

    static public void ExcuteMessageCommend(String command, GenericMessageEvent event, String msg)
    {
        commands.get(command).OnCommand(Instances.get(event.getGuild().getId()), event, msg);
        commands.get(command).OnPostCommand(Instances.get(event.getGuild().getId()), event);
    }
}
