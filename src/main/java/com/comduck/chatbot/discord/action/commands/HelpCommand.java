package com.comduck.chatbot.discord.action.commands;

import com.comduck.chatbot.discord.BotInstance;
import com.comduck.chatbot.discord.action.Command;
import com.comduck.chatbot.discord.action.MessageCommand;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.GenericMessageEvent;

import java.io.File;

@MessageCommand(name = {"help", "hlp"})
public class HelpCommand implements Command {

    @Override
    public void OnCommand(BotInstance instance, GenericEvent e, String msg, boolean isAdd) {
        GenericMessageEvent event = (GenericMessageEvent) e;

        String helpmsg = "***임시 명령어 도움말***\r"+
                "```접두사 : '?' (명령어를 사용할때 가장앞에 반드시 포함되어있어야 합니다)\r"+
                "help : 명령어 도움말 입니다. [hlp로 대체가능]\r\r"+
                "Music\r"+
                "    play [Url또는Title] : 음악을 재생합니다.\r"+
                "    - PlayingDisplay : 재생 메시지의 디자인을 선택합니다.\r"+
                "    pause : 음악을 일시정지 합니다\r"+
                "    join [음성채널ID] : 음성채널에 봇을 추가합니다.(ID가 없으면 요청자가 있는 방에 추가됩니다)\r"+
                "    leave : 음성채널에서 내보냅니다. [out으로 대체가능]\r"+
                "    stop : 대기열을 초기화 합니다.\r"+
                "    skip : 다음곡으로 넘어갑니다. [next로 대체가능]\r"+
                "    volume [0과 100사이값] : 음량을 조절합니다. [vol로 대체가능]\r"+
                "    tracklist : 대기열을 출력합니다. [songlist, tlist, slist, queue, q로 대체가능]\r"+
                "    goto [시간 00:00] : 현재곡의 재생을 지정한 시간으로 이동합니다.\r"+
                "    shuffle : 대기열을 무작위로 섞습니다. [mix, sf로 대체가능]\r"+
                "    repeat : 현재 재생중인 곡을 다시 대기열에 추가합니다. [replay, rp로 대체가능]\r"+
                "    clear : 텍스트채널의 문자 50개씩 삭제합니다.\r" +
                "Favorite\n" +
                "    favorite [Key] : 설정한 별명으로 즐겨찾기의 음악을 재생합니다.\r" +
                "    change [Key] [New Key] : 즐겨찾기 곡의 별명을 변경합니다\r"+
                "Utility\r"+
                "    papago [언어] [텍스트] : 파파고 엔진으로 번역합니다.\r"+
                "    shopping [제품이름] : 네이버 쇼핑에서 상품을 검색합니다. [shop로 대체가능]\r"+
                "    roulette [베팅금액] : 룰렛을 돌립니다.(시작 500만원/최소 천원/최대 만원) [rol로 대체가능]\r"+
                "    hangang : 한강온도를 표시합니다.\r"+
                "    samsung : 현재 삼성전자 주식을 확인합니다. (한강온도표시도 포함)\r"+
                "```\r"+
                "제작 : KIBA#4466\r";
        event.getChannel().sendFile(new File("play_help.png")).queue();
        event.getChannel().sendMessage(helpmsg).queue();
    }

    @Override
    public void OnPostCommand(BotInstance instance, GenericEvent e) {

    }
}
