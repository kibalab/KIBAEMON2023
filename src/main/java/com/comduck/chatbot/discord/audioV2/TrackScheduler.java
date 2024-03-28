package com.comduck.chatbot.discord.audioV2;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class TrackScheduler extends AudioEventAdapter {

    private final BlockingQueue<TrackMessage> queue;
    public TrackMessage playing;
    private final AudioPlayer player;

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<TrackMessage>();
    }

    public void queue(TrackMessage track) {
        if (playing != null) {
            track.Track.stop();
            queue.offer(track);
        }else{
            playing = track;
            player.startTrack(track.Track, true);
        }
    }

    public boolean isEmpty() {
        return this.queue.isEmpty();
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

    public void playNextTrack(boolean noInterrupt)
    {
        var next = queue.poll();

        try {
            next.Track.setPosition(0);
            player.startTrack(next.Track, true);

            var last = playing;
            playing = next;
            if(last != null)last.OnEnd.accept(last);
            playing.OnStart.accept(playing);
        } catch (Exception e) {
            e.printStackTrace();
        }


}

    @Override
    public void onPlayerPause(AudioPlayer player) {
        playing.OnPause.accept(playing);
    }

    @Override
    public void onPlayerResume(AudioPlayer player) {
        playing.OnResume.accept(playing);
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        playing.OnStart.accept(playing);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {

        var ended = playing;
        playing = null;
        if(ended != null) ended.OnEnd.accept(ended);
        if (endReason.mayStartNext && !queue.isEmpty()) {
            playNextTrack(true);
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
        playNextTrack(false);

        exception.printStackTrace();
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        System.out.println("[TrackScheduler] Track is stuck..." + track.getInfo().title);

        player.startTrack(track, false);
    }
}