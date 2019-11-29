package com.comduck.chatbot.discord.minigame;
import net.dv8tion.jda.core.entities.User;

import java.util.LinkedHashMap;
import java.util.Random;


public class Roulette {
    LinkedHashMap<User, Integer> userMoney = new LinkedHashMap();
    LinkedHashMap<User, Integer> bettingMoney = new LinkedHashMap();
    public Roulette() {

    }

    public void betting(User user, int m) {
        if (!userMoney.keySet().contains(user)) {
            userMoney.put(user, 5000000);
        }
        if (m > 10000) {
            m = 10000;
        } else if (m < 1000) {
            m = 1000;
        }

        int um = userMoney.get(user);
        userMoney.put(user, um - m);
        bettingMoney.put(user, m);
    }

    public String[] letRoulette(User user) {


        Random rnd =new Random();
        StringBuffer buf =new StringBuffer();

        String[] r = new String[3];


        double d = rnd.nextInt(101)/10;
        if (true) { //d > 0.05
            String[] icons = {":person_facepalming:", ":eggplant:", ":poop:", ":seven:", ":moneybag:"};
            for(int i=0;i<3;i++) {
                r[i] = icons[rnd.nextInt(3)];
            }

        } /*else if(d > 0.2){
            for(int i=0;i<3;i++) {
                r[i] = ":person_facepalming:";
            }
        }
        else if(d > 0.4){
            for(int i=0;i<3;i++) {
                r[i] = ":eggplant:";
            }
        }
        else if(d > 0.85){
            for(int i=0;i<3;i++) {
                r[i] = ":poop:";
            }
        }
        else if(d > 3.5){
            for(int i=0;i<3;i++) {
                r[i] = ":seven:";
            }
        }
        else {
            for(int i=0;i<3;i++) {
                r[i] = ":moneybag:";
            }
        }*/
        return r;
    }

    public int checkResult(User user, String[] r) {
        int f = 0;
        if (r[0].equals(r[1]) && r[1].equals(r[2])) {
            if(r[0].equals(":person_facepalming:")) f = 2;
            if(r[0].equals(":eggplant:")) f = 4;
            if(r[0].equals(":poop:")) f = 8;
            if(r[0].equals(":seven:")) f = 32;
            if(r[0].equals(":moneybag:")) f = 512;

        }
        int b = bettingMoney.get(user);
        int m = userMoney.get(user);
        userMoney.put(user, m+b*f);

        return f;
    }

    public int getMoney(User user) {
        return userMoney.get(user);
    }
}
