package com.comduck.chatbot.discord.audioV2;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends AudioEventAdapter {

    private final BlockingQueue<AudioTrack> queue;
    private final AudioPlayer player;

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<AudioTrack>();
    }

    public void queue(AudioTrack track) {
        System.out.println("[TrackScheduler] Enqueue : " + track.getInfo().title);
        if (!player.startTrack(track, true)) {
            track.stop();
            queue.offer(track);
        }
    }
    public BlockingQueue getTracks() {
        return queue;
    }

    public void clear() {
        queue.clear();
    }

    public int trackCount() {
        return queue.size();
    }

    public void playNextTrack()
    {
        var next = queue.poll();

        try {
            player.startTrack(next, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPlayerPause(AudioPlayer player) {
        QuickController.UpdateController(player, player.getPlayingTrack());
    }

    @Override
    public void onPlayerResume(AudioPlayer player) {
        QuickController.UpdateController(player, player.getPlayingTrack());
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        QuickController.UpdateController(player, track);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {

        QuickController.RemoveController(track, queue.isEmpty());
        if (endReason.mayStartNext) {
            playNextTrack();
        }

        // endReason == FINISHED: A track finished or died by an exception (mayStartNext = true).
        // endReason == LOAD_FAILED: Loading of a track failed (mayStartNext = true).
        // endReason == STOPPED: The player was stopped.
        // endReason == REPLACED: Another track started playing while this had not finished
        // endReason == CLEANUP: Player hasn't been queried for a while, if you want you can put a
        //                       clone of this back to your queue
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        QuickController.RemoveController(track, queue.isEmpty());
        playNextTrack();
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        QuickController.RemoveController(track, queue.isEmpty());
        playNextTrack();
    }
}