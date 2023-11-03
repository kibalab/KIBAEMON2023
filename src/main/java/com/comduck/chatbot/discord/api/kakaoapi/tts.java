package com.comduck.chatbot.discord.api.kakaoapi;

import java.util.HashMap;

public class tts {

    String url; 	//URL

    public tts() {
        url = "https://kakaoi-newtone-openapi.kakao.com/v1/synthesize";
    }

    public void requestTtsData(String text){
        HashMap<String, String> head = new HashMap<String, String>();

        head.put("Content-Type", "application/octet-stream");
        head.put("Transfer-Encoding", "chunked");
        head.put("Authorization", "f253ff1517e7eca0ecd35fbcb0166d25");

        String resp = post.postRequest(url,  head,null);
    }
}
