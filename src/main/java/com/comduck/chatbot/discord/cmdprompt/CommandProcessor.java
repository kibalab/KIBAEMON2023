package com.comduck.chatbot.discord.cmdprompt;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class CommandProcessor {

    private StringBuffer buffer;
    private Process process;
    private BufferedReader bufferedReader;
    private StringBuffer readBuffer;

    private String inputCommand(String cmd) {

        buffer = new StringBuffer();

        buffer.append("cmd.exe ");
        buffer.append("/c ");
        buffer.append(cmd);

        return buffer.toString();
    }

    private String execCommand(String cmd) {
        try {
            process = Runtime.getRuntime().exec(cmd);
            bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line = null;
            readBuffer = new StringBuffer();

            while((line = bufferedReader.readLine()) != null) {
                readBuffer.append(line);
                readBuffer.append("\n");
            }

            return readBuffer.toString();
        }catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        return null;
    }

    public String Run(String cmd) {
        String command = inputCommand(cmd);
        String result = execCommand(command);
        try {
            String r = new String(result.getBytes(), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        System.out.println(result);
        return result;
    }
}
