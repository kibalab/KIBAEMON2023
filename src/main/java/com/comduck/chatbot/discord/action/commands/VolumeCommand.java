package com.comduck.chatbot.discord.action.commands;

import com.comduck.chatbot.discord.BotInstance;
import com.comduck.chatbot.discord.action.Category;
import com.comduck.chatbot.discord.action.Command;
import com.comduck.chatbot.discord.action.MessageCommand;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;

@MessageCommand(name = {"volume", "vol"}, parm = {"Volume%"}, desc = "플레이어의 음량을 조절합니다.", cat= Category.Audio)
public class VolumeCommand implements Command {

    @Override
    public void OnCommand(BotInstance instance, GenericEvent e, String msg, boolean isAdd) {
        GenericMessageEvent genEvent = (GenericMessageEvent) e;

        //재생되고 있는 트랙이 있는지 확인
        if (instance.playerInstance.player.getPlayingTrack() == null) {
            return;
        }

        //파라미터 분리
        String _Nvol = msg.replaceFirst("volume ", "");

        //현재 음량과 요청 음량을 가져옴
        int Ovol = instance.playerInstance.player.getVolume();
        int Nvol = Integer.parseInt(_Nvol);

        //최대 음량 제한
        if (Nvol > 100) {
            Nvol = 100;
        }

        genEvent.getChannel().sendMessage(String.format(
                "> 음량 제어 %d->%s",
                Ovol,
                Nvol
        )).queue();

        //1.다음 곡도 같은 음량을 유지하기 위해 전역변수에 담음
        //2.플레이어 음량 변경
        instance.globalVolume = Nvol;
        instance.playerInstance.player.setVolume(instance.globalVolume);
    }

    @Override
    public void OnPostCommand(BotInstance instance, GenericEvent e) {

    }
}
