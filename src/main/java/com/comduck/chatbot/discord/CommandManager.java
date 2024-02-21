package com.comduck.chatbot.discord;

import com.comduck.chatbot.discord.action.ReactionCommand;
import com.comduck.chatbot.discord.audiocore.GuildMusicManager;
import com.comduck.chatbot.discord.audiocore.PlayerManager;
import com.comduck.chatbot.discord.audiocore.TrackScheduler;
import com.comduck.chatbot.discord.action.Command;
import com.comduck.chatbot.discord.action.MessageCommand;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import org.reflections.Reflections;
import se.michaelthelin.spotify.SpotifyApi;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.*;

public class CommandManager {

    static public String CommandPackage = "com.comduck.chatbot.discord.action.commands";
    static public String ReactionPackage = "com.comduck.chatbot.discord.action.reactions";

    static public HashMap<String, Command> commands = new HashMap<>();
    static public HashMap<String, Command> reactions = new HashMap<>();

    static public void LoadAllCommands() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, ClassNotFoundException, MalformedURLException {
        Reflections reflections = new Reflections(CommandPackage);
        Set<Class<? extends Command>> classes = reflections.getSubTypesOf(Command.class);
        reflections = new Reflections(ReactionPackage);
        classes.addAll(reflections.getSubTypesOf(Command.class));

        System.out.println("[ ACTIONS ]");
        for (Class<? extends Command> p : classes) {

            MessageCommand mc = p.getDeclaredAnnotation(MessageCommand.class);
            ReactionCommand rc = p.getDeclaredAnnotation(ReactionCommand.class);

            if(mc != null)
            {
                Command cmdObjecct = p.getConstructor().newInstance();

                for (String cmd: mc.name()) {
                    AddCommand(cmd, cmdObjecct);
                }
            }
            if(rc != null)
            {
                Command cmdObjecct = p.getConstructor().newInstance();

                for (String cmd: rc.name()) {
                    AddReaction(cmd, cmdObjecct);
                }
            }
        }
    }

    static public void AddCommand(String cmd, Command command) {
        System.out.println("ADD COMMAND " + cmd + " | " + command.getClass().getName());
        commands.put(cmd, command);
    }

    static public void AddReaction(String cmd, Command command) {
        System.out.println("ADD REACTION :" + cmd + ": | " + command.getClass().getName());
        reactions.put(cmd, command);
    }

    static public void ExcuteMessageCommend(String command, GenericMessageEvent event, String msg)
    {
        try {
            commands.get(command).OnCommand(BotInstance.getInstance(event.getGuild().getId()), event, msg, true);
            commands.get(command).OnPostCommand(BotInstance.getInstance(event.getGuild().getId()), event);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static public void ExcuteReactionCommend(MessageReaction.ReactionEmote reaction, GenericMessageEvent event, boolean isAdd)
    {
        try {
            reactions.get(reaction.getEmoji()).OnCommand(BotInstance.getInstance(event.getGuild().getId()), event, "", isAdd);
            reactions.get(reaction.getEmoji()).OnPostCommand(BotInstance.getInstance(event.getGuild().getId()), event);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
