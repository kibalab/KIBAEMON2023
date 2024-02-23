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
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import org.apache.hc.core5.http.ParseException;
import org.reflections.Reflections;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.sql.SQLException;
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
        if(!commands.containsKey(command)) {
            System.out.printf("[CommandManager] Command(%s) Not Founded\n", command);
            return;
        }
        try {
            commands.get(command).OnCommand(BotInstance.getInstance(event.getGuild().getId()), event, msg, true);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } catch (SpotifyWebApiException e) {
            throw new RuntimeException(e);
        }
        commands.get(command).OnPostCommand(BotInstance.getInstance(event.getGuild().getId()), event);
    }

    static public void ExcuteMessageCommend(String command, ButtonInteractionEvent event, String msg)
    {
        if(!commands.containsKey(command)) {
            System.out.printf("[CommandManager] Command(%s) Not Founded\n", command);
            return;
        }
        try {
            commands.get(command).OnCommand(BotInstance.getInstance(event.getGuild().getId()), event, msg, true);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } catch (SpotifyWebApiException e) {
            throw new RuntimeException(e);
        }
        commands.get(command).OnPostCommand(BotInstance.getInstance(event.getGuild().getId()), event);
    }

    static public void ExcuteReactionCommend(MessageReaction reaction, GenericMessageEvent event, boolean isAdd)
    {
        if(!reactions.containsKey(reaction.getEmoji().getAsReactionCode())) {
            System.out.printf("[CommandManager] Raction(%s) Not Founded\n", reaction.getEmoji().getAsReactionCode());
            return;
        }

        try {
            reactions.get(reaction.getEmoji().getAsReactionCode()).OnCommand(BotInstance.getInstance(event.getGuild().getId()), event, "", isAdd);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        } catch (SpotifyWebApiException e) {
            throw new RuntimeException(e);
        }
        reactions.get(reaction.getEmoji().getAsReactionCode()).OnPostCommand(BotInstance.getInstance(event.getGuild().getId()), event);
    }
}
