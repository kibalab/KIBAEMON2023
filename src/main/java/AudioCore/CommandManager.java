package AudioCore;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.GenericMessageEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.core.managers.AudioManager;

import java.util.ArrayList;
import java.util.List;

public class CommandManager {
    GuildMusicManager musicManager; // = manager.getGuildMusicManager(event.getGuild());
    AudioPlayer player; // = musicManager.player;
    TrackScheduler scheduler; // = musicManager.scheduler;

    List<PostCommandListener> postCommandListeners;

    public CommandManager(GuildMusicManager guildMusicManager, AudioPlayer player, TrackScheduler scheduler) {
        this.musicManager = guildMusicManager;
        this.player = player;
        this.scheduler = scheduler;
        this.postCommandListeners = new ArrayList<>();
    }

    public void addPostCommandListener(PostCommandListener listener) {
        this.postCommandListeners.add(listener);
    }

    public void removePostCommandListener(PostCommandListener listener) {
        this.postCommandListeners.remove(listener);
    }

    public void playCommand() {

    }

    private void raisePostCommand(GenericMessageEvent event) {
        for (PostCommandListener listener : postCommandListeners) {
            listener.onPostCommand(event);
        }
    }

    public void stopCommand(GenericMessageEvent event) {
        if (event instanceof MessageReceivedEvent) {
            MessageReceivedEvent msgEvent = (MessageReceivedEvent)event;

            msgEvent.getChannel().sendMessage(String.format(
                    "> 대기열 재생 중지 ``%s``",
                    msgEvent.getAuthor().getName()
            )).queue();
        } else if (event instanceof GenericMessageReactionEvent) {
            GenericMessageReactionEvent reactionEvent = (GenericMessageReactionEvent)event;

            reactionEvent.getChannel().sendMessage(String.format(
                    "> 대기열 재생 중지 ``%s``",
                    reactionEvent.getUser().getName()
            )).queue();
        }

        AudioPlayer player = musicManager.player;
        TrackScheduler scheduler = musicManager.scheduler;

        scheduler.getQueue().clear();
        player.stopTrack();
        player.setPaused(false);

        raisePostCommand(event);
    }
}
