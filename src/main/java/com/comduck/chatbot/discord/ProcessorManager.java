package com.comduck.chatbot.discord;

import com.comduck.chatbot.discord.action.Permission;
import com.comduck.chatbot.discord.action.Permissions;
import com.comduck.chatbot.discord.action.Processor;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProcessorManager {

    static Set<Processor> processors = new HashSet<>();

    static public String ProcessorPackage = "com.comduck.chatbot.discord.action.processor";

    static public void LoadAllCommands() throws InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        Reflections reflections = new Reflections(ProcessorPackage);
        Set<Class<? extends Processor>> classes = reflections.getSubTypesOf(Processor.class);

        System.out.println("[ Processor ]");
        for (Class<? extends Processor> p : classes) {
            System.out.println(p.getName());
            processors.add(p.getConstructor().newInstance());
        }
    }

    static public void ExcuteMessageProcessor(GenericMessageEvent event, String msg)
    {
        for (Processor p : processors)
        {
            var annos = p.getClass().getDeclaredAnnotation(Permissions.class);
            var anno = p.getClass().getDeclaredAnnotation(Permission.class);

            boolean excutable = false;

            List<Permission> permissions = new ArrayList<>();
            if(annos != null) permissions.addAll(List.of(annos.value()));
            if(anno != null) permissions.add(anno);

            for (Permission perm: permissions)
                if(perm.guildId().equals(event.getGuild().getId()) || perm.guildId().isEmpty())
                    if(perm.channelId().equals(event.getChannel().getId()) || perm.channelId().isEmpty())
                        if(perm.userId().equals(((MessageReceivedEvent) event).getAuthor().getId()) || perm.userId().isEmpty())
                            excutable = true;

            if(excutable) p.OnProcess(event, msg);
        }
    }
}
