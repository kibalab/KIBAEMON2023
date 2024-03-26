package com.comduck.chatbot.discord.audioV2;

import com.comduck.chatbot.discord.ActionManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.GenericEvent;

import java.util.function.Consumer;

public class TrackMessage {

    public TrackMessage(Message _msg, GenericEvent _event, AudioTrack _track)
    {
        Message = _msg;
        Event = _event;
        Track = _track;
        
        OnStart = ((trackMessage -> {
            System.out.println("[TrackMessage] OnStart : " + trackMessage.Track.getInfo().title);
            ActionManager.AttachUserAction("play", trackMessage.Message);
        }));

        OnPause = ((trackMessage -> {
            System.out.println("[TrackMessage] OnPause : " + trackMessage.Track.getInfo().title);
            ActionManager.AttachUserAction("play", trackMessage.Message);
        }));

        OnResume = ((trackMessage -> {
            System.out.println("[TrackMessage] OnResume : " + trackMessage.Track.getInfo().title);
            ActionManager.AttachUserAction("play", trackMessage.Message);
        }));

        OnEnd = ((trackMessage -> {
            System.out.println("[TrackMessage] OnEnd : " + trackMessage.Track.getInfo().title);
            ActionManager.AttachUserAction("play", trackMessage.Message);
        }));
    }

    public final Message Message;
    public final GenericEvent Event;
    public final AudioTrack Track;

    public Consumer<TrackMessage> OnStart;
    public Consumer<TrackMessage> OnPause;
    public Consumer<TrackMessage> OnResume;
    public Consumer<TrackMessage> OnEnd;
}
