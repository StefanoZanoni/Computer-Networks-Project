package winsomeServer.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;
import winsome.base.Post;
import winsome.base.User;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class StateWriter extends TimerTask implements Runnable {

    private final Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

    private final String usersDirPath;
    private final String users_networkDirPath;
    private final String postsDirPath;
    private final String posts_networkDirPath;
    private final String tags_networkDirPath;
    public StateWriter(String[] paths) {

        if (paths == null)
            throw new NullPointerException();
        if (paths.length != 5)
            throw new IllegalArgumentException();

        usersDirPath = paths[0];
        users_networkDirPath = paths[1];
        postsDirPath = paths[2];
        posts_networkDirPath = paths[3];
        tags_networkDirPath = paths[4];

        // create directories
        Path usersDir = Paths.get(usersDirPath).toAbsolutePath();
        try {
            Files.createDirectories(usersDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Path usersNetworkDir = Paths.get(users_networkDirPath).toAbsolutePath();
        try {
            Files.createDirectories(usersNetworkDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Path postsDir = Paths.get(postsDirPath).toAbsolutePath();
        try {
            Files.createDirectories(postsDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Path postsNetworkDir = Paths.get(posts_networkDirPath).toAbsolutePath();
        try {
            Files.createDirectories(postsNetworkDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Path tagsNetworkDir = Paths.get(tags_networkDirPath).toAbsolutePath();
        try {
            Files.createDirectories(tagsNetworkDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void run() {

        String usersPath = usersDirPath.concat("users.json");
        Type mapStringUser = new TypeToken<ConcurrentHashMap<String, User>>() {}.getType();
        writeJson(usersPath, SocialNetworkManager.users, mapStringUser);

        String usersNetworkPath = users_networkDirPath.concat("usersNetwork.json");
        Type mapStringListOfString = new TypeToken<ConcurrentHashMap<String, List<String>>>() {}.getType();
        writeJson(usersNetworkPath, SocialNetworkManager.usersNetwork, mapStringListOfString);

        String postsPath = postsDirPath.concat("posts.json");
        Type mapIntegerPost = new TypeToken<ConcurrentHashMap<Integer, Post>>() {}.getType();
        writeJson(postsPath, SocialNetworkManager.posts, mapIntegerPost);

        String postsNetworkPath = posts_networkDirPath.concat("postsNetwork.json");
        Type mapStringListOfInteger = new TypeToken<ConcurrentHashMap<String, List<Integer>>>() {}.getType();
        writeJson(postsNetworkPath, SocialNetworkManager.postsNetwork, mapStringListOfInteger);

        String tagsNetworkPath = tags_networkDirPath.concat("tagsNetwork.json");
        writeJson(tagsNetworkPath, SocialNetworkManager.tagsNetwork, mapStringListOfString);

    }

    private <K, V> void writeJson(String filepath, Map<K, V> map, Type mapType) {

        try ( Writer writer = new FileWriter(filepath, StandardCharsets.UTF_8) ) {
            JsonWriter jsonWriter = new JsonWriter(writer);
            jsonWriter.setIndent("\n");
            gson.toJson(map, mapType, jsonWriter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}