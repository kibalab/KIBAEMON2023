package AudioCore;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.guild.GuildReadyEvent;
import net.dv8tion.jda.core.events.message.GenericMessageEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.core.managers.AudioManager;
import org.apache.log4j.Logger;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

public class CommandManager {


    GuildMusicManager musicManager; // = manager.getGuildMusicManager(event.getGuild());
    AudioPlayer player; // = musicManager.player;
    TrackScheduler scheduler; // = musicManager.scheduler;

    List<PostCommandListener> postCommandListeners;

    int globalVolume = 10;

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

    private void raisePostCommand(GenericMessageEvent event) {
        for (PostCommandListener listener : postCommandListeners) {
            listener.onPostCommand(event);
        }
    }

    public void playCommand(GenericMessageEvent event, String msg) {
        VoiceChannel Vch = null;
        String url = "";
        if (event instanceof MessageReceivedEvent) {
            MessageReceivedEvent msgEvent = (MessageReceivedEvent) event;

            url = msg.replaceFirst("play ", "");

            Vch = ((MessageReceivedEvent) event).getMember().getVoiceState().getChannel();
        } else if (event instanceof GenericMessageReactionEvent) {
            GenericMessageReactionEvent reactionEvent = (GenericMessageReactionEvent) event;

            url = this.player.getPlayingTrack().getInfo().uri;

            Vch = ((GenericMessageReactionEvent) event).getMember().getVoiceState().getChannel();
        }

        AudioManager audiomng = event.getGuild().getAudioManager();
        audiomng.openAudioConnection(Vch);

        PlayerManager manager = PlayerManager.getInstance();
        manager.loadAndPlay(event, url);
        manager.getGuildMusicManager(event.getGuild()).player.setVolume(globalVolume);

        raisePostCommand(event);
    }

    public void joinCommand(GenericMessageEvent event) {
        VoiceChannel Vch = null;
        if (event instanceof MessageReceivedEvent) {
            Vch = ((MessageReceivedEvent) event).getMember().getVoiceState().getChannel();
            if (event.getGuild().getName() == "Nerine force") {
                if (Vch.getName() != "Music") {
                    return;
                }
            }
            event.getChannel().sendMessage(String.format(
                    "> %s 입장 ``%s``",
                    Vch.getName(),
                    ((MessageReceivedEvent) event).getAuthor().getName()
            )).queue();
        }
        AudioManager audiomng = event.getGuild().getAudioManager();
        audiomng.openAudioConnection(Vch);
    }

    public void leaveCommand(GenericMessageEvent event) {
        VoiceChannel Vch = null;
        if (event instanceof MessageReceivedEvent) {
            Vch = ((MessageReceivedEvent) event).getMember().getVoiceState().getChannel();
            event.getChannel().sendMessage(String.format(
                    "> %s 퇴장 ``%s``",
                    Vch.getName(),
                    ((MessageReceivedEvent) event).getAuthor().getName()
            )).queue();
        }
        event.getGuild().getAudioManager().closeAudioConnection();

        this.scheduler.getQueue().clear();
        this.player.stopTrack();
        this.player.setPaused(false);
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

        this.scheduler.getQueue().clear();
        this.player.stopTrack();
        this.player.setPaused(false);

        raisePostCommand(event);
    }

    public void skipCommand(GenericMessageEvent event) {
        if (event instanceof MessageReceivedEvent) {
            MessageReceivedEvent msgEvent = (MessageReceivedEvent) event;
            if (this.player.getPlayingTrack() == null) {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setColor(new Color(0xff6624));
                eb.addField("경고 Warning", String.format(
                        "대기열이 비어 있습니다.\n``%s``",
                        ((MessageReceivedEvent) event).getAuthor().getName().toString()
                ), false);
                event.getChannel().sendMessage(eb.build()).queue();
            } else {
                event.getChannel().sendMessage(String.format(
                        "> 곡 스킵 ``%s``",
                        ((MessageReceivedEvent) event).getAuthor().getName()
                )).queue();
                this.scheduler.nextTrack();
            }
        } else if (event instanceof GenericMessageReactionEvent) {
            GenericMessageReactionEvent reactionEvent = (GenericMessageReactionEvent) event;
            if (this.player.getPlayingTrack() == null) {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setColor(new Color(0xff6624));
                eb.addField("경고 Warning", String.format(
                        "대기열이 비어 있습니다.\n``%s``",
                        ((GenericMessageReactionEvent) event).getUser().getName().toString()
                ), false);
                event.getChannel().sendMessage(eb.build()).queue();
            } else {
                event.getChannel().sendMessage(String.format(
                        "> 곡 스킵 ``%s``",
                        ((GenericMessageReactionEvent) event).getUser().getName()
                )).queue();
                this.scheduler.nextTrack();
            }
        }
    }

    public void volumeCommand(GenericMessageEvent event, String msg) {
        String _Nvol = msg.replaceFirst("volume ", "");

        int Ovol = this.player.getVolume();
        int Nvol = Integer.parseInt(_Nvol);

        if (Nvol > 100) {
            Nvol = 100;
        }

        event.getChannel().sendMessage(String.format(
                "> 음량 제어 %d->%s",
                Ovol,
                Nvol
        )).queue();
        globalVolume = Nvol;
        this.player.setVolume(globalVolume);
    }

    public void tracklistCommand(GenericMessageEvent event) {
        if (player.getPlayingTrack() == null) {
            EmbedBuilder eb = new EmbedBuilder();
            eb.setColor(new Color(0xff6624));
            if (event instanceof MessageReceivedEvent) {
                MessageReceivedEvent msgEvent = (MessageReceivedEvent) event;
                eb.addField("경고 Warning", String.format(
                        "대기열이 비어 있습니다.\n``%s``",
                        ((MessageReceivedEvent) event).getAuthor().getName().toString()
                ), false);
            } else if (event instanceof GenericMessageReactionEvent) {
                GenericMessageReactionEvent reactionEvent = (GenericMessageReactionEvent) event;
                eb.addField("경고 Warning", String.format(
                        "대기열이 비어 있습니다.\n``%s``",
                        ((GenericMessageReactionEvent) event).getUser().getName().toString()
                ), false);
            }
            event.getChannel().sendMessage(eb.build()).queue();
        } else {
            AudioTrackInfo info = player.getPlayingTrack().getInfo();

            EmbedBuilder eb = new EmbedBuilder();
            eb.setColor(new Color(0x244aff));
            eb.setTitle(String.format(
                    "재생중 - %s [%s/%s]",
                    info.title,
                    formatTime(player.getPlayingTrack().getPosition()),
                    formatTime(player.getPlayingTrack().getDuration())
            ));
            List playelist = new ArrayList(scheduler.getQueue());
            String str = "";
            if (playelist.size() != 0) {
                for (int i = 0; true; i++) {
                    if (playelist.size() == i) {
                        break;
                    }
                    AudioTrack t = (AudioTrack) playelist.get(i);
                    str += String.format("%d. %s\n", i + 1, t.getInfo().title);
                }
            } else {
                str = "None";
            }
            eb.addField("TrackList", str, false);
            event.getChannel().sendMessage(eb.build()).queue();
        }
    }

    public void gotoCommand(GenericMessageEvent event, String msg) {
        msg = msg.replaceFirst("goto ", "");

        if (this.player.getPlayingTrack() == null) {
            EmbedBuilder eb = new EmbedBuilder();
            eb.setColor(new Color(0xff6624));
            if (event instanceof MessageReceivedEvent) {
                MessageReceivedEvent msgEvent = (MessageReceivedEvent) event;
                eb.addField("경고 Warning", String.format(
                        "대기열이 비어 있습니다.\n``%s``",
                        ((MessageReceivedEvent) event).getAuthor().getName().toString()
                ), false);
            } else if (event instanceof GenericMessageReactionEvent) {
                GenericMessageReactionEvent reactionEvent = (GenericMessageReactionEvent) event;
                eb.addField("경고 Warning", String.format(
                        "대기열이 비어 있습니다.\n``%s``",
                        ((GenericMessageReactionEvent) event).getUser().getName().toString()
                ), false);
            }
            event.getChannel().sendMessage(eb.build()).queue();
            return;
        } else {
            long time = formatLong(msg);
            System.out.println(time);
            this.player.getPlayingTrack().setPosition(time);

        }
    }

    public void shuffleCommand(GenericMessageEvent event) {
        Queue queue = scheduler.getQueue();
        List<AudioTrack> list = new ArrayList<>();

        for (int i = 0; true; i++) {
            list.add((AudioTrack) queue.poll());
            if (queue.size() == 0) {
                break;
            }
        }

        Collections.shuffle(list);

        for (int i = 0; true; i++) {
            queue.offer(list.get(i));
            if (queue.size() == list.size()) {
                break;
            }
        }
        if (event instanceof MessageReceivedEvent) {
            MessageReceivedEvent msgEvent = (MessageReceivedEvent) event;
            event.getChannel().sendMessage(String.format("> 대기열 셔플 ``%s``", ((MessageReceivedEvent) event).getAuthor().getName())).queue();
        } else if (event instanceof GenericMessageReactionEvent) {
            GenericMessageReactionEvent reactionEvent = (GenericMessageReactionEvent) event;
            event.getChannel().sendMessage(String.format("> 대기열 셔플 ``%s``", ((GenericMessageReactionEvent) event).getUser().getName())).queue();
        }
    }

    public void repeatCommand(GenericMessageEvent event) {
        String msg = "play " + this.player.getPlayingTrack().getInfo().uri;
        playCommand(event, msg);
        if (event instanceof MessageReceivedEvent) {
            MessageReceivedEvent msgEvent = (MessageReceivedEvent) event;
            event.getChannel().sendMessage(String.format("> 현재곡 재등록 ``%s``", ((MessageReceivedEvent) event).getAuthor().getName())).queue();
        } else if (event instanceof GenericMessageReactionEvent) {
            GenericMessageReactionEvent reactionEvent = (GenericMessageReactionEvent) event;
            event.getChannel().sendMessage(String.format("> 현재곡 재등록 ``%s``", ((GenericMessageReactionEvent) event).getUser().getName())).queue();
        }
    }

    private String formatTime(long time) {
        final long h = time / TimeUnit.HOURS.toMillis(1);
        final long m = time % TimeUnit.HOURS.toMillis(1) / TimeUnit.MINUTES.toMillis(1);
        final long s = time % TimeUnit.MINUTES.toMillis(1) / TimeUnit.SECONDS.toMillis(1);

        if (h != 0) {
            return String.format("%2d:%2d:%2d", h, m, s);
        } else {
            return String.format("%2d:%2d", m, s);
        }
    }

    private long formatLong(String msg) {
        String[] StrTime = msg.split(":");
        long LongTime = 0;
        if (StrTime.length == 3) {
            LongTime += Long.parseLong(StrTime[0]) * 60 * 60;
            LongTime += Long.parseLong(StrTime[1]) * 60;
            LongTime += Long.parseLong(StrTime[2]);
        } else if (StrTime.length == 2) {
            LongTime += Long.parseLong(StrTime[0]) * 60;
            LongTime += Long.parseLong(StrTime[1]);
        } else {
            LongTime += Long.parseLong(StrTime[1]);
        }

        return LongTime * 1000;
    }
}
