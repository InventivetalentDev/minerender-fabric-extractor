package org.minerender.extractor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Output {

    public static ScheduledExecutorService SCHEDULER = Executors.newSingleThreadScheduledExecutor();
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    static {
        SCHEDULER.schedule(Output::writeAll, 30, TimeUnit.SECONDS);
    }

    static Map<String, JsonObject> BUFFER = new ConcurrentHashMap<>();

    public static void writeAll() {
        for (String key : BUFFER.keySet()) {
            write(key);
        }
    }

    public static void write(String name) {
        if (BUFFER.containsKey(name)) {
            write(name, BUFFER.get(name));
            BUFFER.remove(name);
        }
    }

    public static void write(String name, JsonObject json) {
        try {
            File file = new File(name + ".json");
            file.createNewFile();

            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                GSON.toJson(json, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static JsonObject append(String name, String key, JsonElement value) {
        JsonObject obj = BUFFER.computeIfAbsent(name, k -> new JsonObject());
        obj.add(key, value);
        return obj;
    }

}
