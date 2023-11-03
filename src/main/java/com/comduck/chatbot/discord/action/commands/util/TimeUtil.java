package com.comduck.chatbot.discord.action.commands.util;

import java.util.concurrent.TimeUnit;

public class TimeUtil {
    // Long 타입 프레임을 String 타입 시간으로 환산
    static public String formatTime(long time) {
        final long h = time / TimeUnit.HOURS.toMillis(1);
        final long m = time % TimeUnit.HOURS.toMillis(1) / TimeUnit.MINUTES.toMillis(1);
        final long s = time % TimeUnit.MINUTES.toMillis(1) / TimeUnit.SECONDS.toMillis(1);

        if (h != 0) {
            return String.format("%02d:%02d:%02d", h, m, s);
        } else {
            return String.format("%02d:%02d", m, s);
        }
    }

    public static long formatLong(String msg) {
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
