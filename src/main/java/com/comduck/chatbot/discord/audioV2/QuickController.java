package com.comduck.chatbot.discord.audioV2;

import com.comduck.chatbot.discord.CommandManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

import java.util.HashMap;
import java.util.List;

public class QuickController {
    public static void UpdateController(AudioPlayer player, AudioTrack track)
    {
        var customData = new HashMap<String, String>();
        customData.put("trackId", track.getIdentifier());

        var msg = ((Message)track.getUserData(HashMap.class).get("send_msg"));
        ActionRow updatedActionRow = ActionRow.of(List.of(new Button[]{
                (Button) CommandManager.BuildAction("play", msg.getGuild(), customData),
                Button.primary("pause " + track.getIdentifier(), "일시정지").withStyle(ButtonStyle.SECONDARY),
                Button.primary("stop " + track.getIdentifier(), "정지").withStyle(ButtonStyle.SECONDARY),
                Button.primary("skip " + track.getIdentifier(), "스킵").withStyle(ButtonStyle.SECONDARY),
                Button.primary("track " + track.getIdentifier(), "대기열").withStyle(ButtonStyle.SECONDARY)
        }));

        msg.editMessageComponents(List.of(updatedActionRow)).queue();
    }

    public static void RemoveController(AudioTrack track, boolean isEmpty)
    {
        if(!isEmpty) {
            var msg = ((Message) track.getUserData(HashMap.class).get("send_msg"));
            List<ActionRow> emptyActionRows = List.of();
            msg.editMessageComponents(emptyActionRows).queue();
        }
        else {
            var msg = ((Message)track.getUserData(HashMap.class).get("send_msg"));
            ActionRow updatedActionRow = ActionRow.of(List.of(new Button[]{
                    Button.primary("play ", "곡 추가")
            }));
            msg.editMessageComponents(List.of(updatedActionRow)).queue();
        }
    }

    public static void RemoveController(Message msg)
    {
        List<ActionRow> emptyActionRows = List.of();
        msg.editMessageComponents(emptyActionRows).queue();
    }
}
