package com.comduck.chatbot.discord;

import java.io.File;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;

public class ResourceManager {

    static public File ResourcesPath = new File("./src/main/resources");

    static private final HashMap<String, File> _resources = new HashMap<String, File>();


    static public void loadAll()
    {
        /*
        System.out.println("[ Resources List ]");
        for (final File file: ResourcesPath.listFiles()) {
            _resources.put(file.getName(), file);
            System.out.println(file.getName());
        }
        */
    }

    static public File getResources(String name) {
        return _resources.get(name);
    }
}
