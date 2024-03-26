package com.comduck.chatbot.discord;

import com.comduck.chatbot.discord.action.*;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.internal.interactions.component.ButtonImpl;
import org.apache.hc.core5.http.ParseException;
import org.reflections.Reflections;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.*;

public class ActionManager {

    static public String CommandPackage = "com.comduck.chatbot.discord.action.commands";
    static public String ReactionPackage = "com.comduck.chatbot.discord.action.reactions";
    static public String UserActionPackage = "com.comduck.chatbot.discord.action.useraction";

    static public HashMap<String, Command> commands = new HashMap<>();
    static public HashMap<String, Command> reactions = new HashMap<>();
    static public HashMap<UserActionMethod, UserAction> userActions = new HashMap<>();

    static public void LoadAllActions() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, ClassNotFoundException, MalformedURLException {
        Reflections reflections = new Reflections(CommandPackage);
        Set<Class<? extends IAction>> classes = reflections.getSubTypesOf(IAction.class);
        reflections = new Reflections(ReactionPackage);
        classes.addAll(reflections.getSubTypesOf(IAction.class));
        reflections = new Reflections(UserActionPackage);
        classes.addAll(reflections.getSubTypesOf(IAction.class));

        System.out.println("[ ACTIONS ]");
        for (Class<? extends IAction> p : classes) {

            MessageCommand mc = p.getDeclaredAnnotation(MessageCommand.class);
            ReactionCommand rc = p.getDeclaredAnnotation(ReactionCommand.class);
            UserActionMethod ua = p.getDeclaredAnnotation(UserActionMethod.class);

            if(mc != null)
            {
                Command cmdObjecct = (Command) p.getConstructor().newInstance();

                for (String cmd: mc.name()) {
                    AddCommand(cmd, cmdObjecct);
                }
            }
            if(rc != null)
            {
                Command cmdObjecct = (Command) p.getConstructor().newInstance();

                for (String cmd: rc.name()) {
                    AddReaction(cmd, cmdObjecct);
                }
            }
            if(ua != null)
            {
                UserAction actionObjecct = (UserAction) p.getConstructor().newInstance();

                for (String cmd: ua.command()) {
                    AddUserAction(ua, actionObjecct);
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

    static public void AddUserAction(UserActionMethod cmd, UserAction command) {
        System.out.println("ADD USER_ACTION <" + cmd + "> | " + command.getClass().getName());
        userActions.put(cmd, command);
    }

    static public void ExcuteMessageCommend(String command, GenericMessageEvent event, String msg, boolean isUserAction)
    {
        if(!commands.containsKey(command)) {
            System.out.printf("[CommandManager] Command(%s) Not Founded\n", command);
            return;
        }
        try {
            commands.get(command).OnCommand(BotInstance.getInstance(event.getGuild().getId()), event, msg, isUserAction);
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

    static public void AttachUserAction(String command, Message msg)
    {
        var rows = msg.getActionRows();

        if(rows.isEmpty()) {
            List<ItemComponent> components = new ArrayList<>();
            userActions.forEach((key, value) -> {
                for (String _command : key.command())
                {
                    if(_command.contains(command))
                    {
                        try
                        {
                            var component = value.Build(msg.getGuild(), msg);
                            if(component != null) components.add(component);
                        }
                        catch (Exception ex)
                        {
                            ex.printStackTrace();
                        }
                    }
                }
            });
            if(components.size() <= 0) {
                List<ActionRow> emptyActionRows = List.of();
                msg.editMessageComponents(emptyActionRows).queue();
            }
            else msg.editMessage(msg.getContentRaw()).setComponents(ActionRow.of(components)).queue();
            return;
        }


        var actionRow = rows.get(0).getComponents();

        for (var j = 0; j < actionRow.size(); j++) {
            var x = actionRow.get(j);
            if(x.getClass() == ButtonImpl.class)
            {
                var button = ((Button) x);
                int finalI = j;
                userActions.forEach((key, value) -> {
                    if(button.getId().equals(key.buttonId()))
                    {
                        var item = value.OnChangeStatus(msg.getGuild(), msg, button);
                        if(item != null) actionRow.set(finalI, item);
                        else actionRow.remove(finalI);
                    }
                });
            }
        }

        msg.editMessage(msg.getContentRaw()).setActionRow(actionRow).queue();
    }

    static public void ExcuteButtonAction(String command, ButtonInteractionEvent event, String msg)
    {
        for (Map.Entry<UserActionMethod, UserAction> entry : userActions.entrySet()) {
            UserActionMethod keys = entry.getKey();
            UserAction value = entry.getValue();

            if (keys.buttonId().equals(command)){
                value.OnClick(event);
            }
        }
        System.out.printf("[CommandManager] Action(%s) Not Founded\n", command);
    }

    public static void ExcuteModalAction(String command, ModalInteractionEvent event, String msg) {
        for (Map.Entry<UserActionMethod, UserAction> entry : userActions.entrySet()) {
            UserActionMethod keys = entry.getKey();
            UserAction value = entry.getValue();

            if (keys.modalId().equals(command)){
                value.OnApply(event);
            }
        }
        System.out.printf("[CommandManager] Action(%s) Not Founded\n", command);
    }

    static public void ExcuteReactionCommend(MessageReaction reaction, GenericMessageEvent event, boolean isUserAction)
    {
        if(!reactions.containsKey(reaction.getEmoji().getAsReactionCode())) {
            System.out.printf("[CommandManager] Raction(%s) Not Founded\n", reaction.getEmoji().getAsReactionCode());
            return;
        }

        try {
            reactions.get(reaction.getEmoji().getAsReactionCode()).OnCommand(BotInstance.getInstance(event.getGuild().getId()), event, "", isUserAction);
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
