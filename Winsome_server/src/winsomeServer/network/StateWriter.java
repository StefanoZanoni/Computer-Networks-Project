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
import java.util.Queue;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This class is used to save the server state to the file system periodically and when the server is turned off.
 */
public class StateWriter extends TimerTask implements Runnable {

    private final Gson gson = new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

    private final String usersDirPath;
    private final String users_networkDirPath;
    private final String postsDirPath;
    private final String posts_networkDirPath;
    private final String tags_networkDirPath;
    private final String removed_IDsPath;

    public StateWriter(String[] paths) throws IOException {

        if (paths == null)
            throw new NullPointerException();
        if (paths.length != 6)
            throw new IllegalArgumentException();

        usersDirPath = paths[0];
        users_networkDirPath = paths[1];
        postsDirPath = paths[2];
        posts_networkDirPath = paths[3];
        tags_networkDirPath = paths[4];
        removed_IDsPath = paths[5];

        Path usersDir = Paths.get(usersDirPath).toAbsolutePath();
        Files.createDirectories(usersDir);

        Path usersNetworkDir = Paths.get(users_networkDirPath).toAbsolutePath();
        Files.createDirectories(usersNetworkDir);

        Path postsDir = Paths.get(postsDirPath).toAbsolutePath();
        Files.createDirectories(postsDir);

        Path postsNetworkDir = Paths.get(posts_networkDirPath).toAbsolutePath();
        Files.createDirectories(postsNetworkDir);

        Path tagsNetworkDir = Paths.get(tags_networkDirPath).toAbsolutePath();
        Files.createDirectories(tagsNetworkDir);

        Path removedIDsDir = Paths.get(removed_IDsPath).toAbsolutePath();
        Files.createDirectories(removedIDsDir);

    }

    @Override
    public void run() {

        String usersPath = usersDirPath.concat("users.json");
        Type mapStringUser = new TypeToken<ConcurrentHashMap<String, User>>() {}.getType();
        writeJsonMap(usersPath, SocialNetworkManager.users, mapStringUser);

        String usersNetworkPath = users_networkDirPath.concat("usersNetwork.json");
        Type mapStringListOfString = new TypeToken<ConcurrentHashMap<String, List<String>>>() {}.getType();
        writeJsonMap(usersNetworkPath, SocialNetworkManager.usersNetwork, mapStringListOfString);

        String postsPath = postsDirPath.concat("posts.json");
        Type mapIntegerPost = new TypeToken<ConcurrentHashMap<Integer, Post>>() {}.getType();
        writeJsonMap(postsPath, SocialNetworkManager.posts, mapIntegerPost);

        String postsNetworkPath = posts_networkDirPath.concat("postsNetwork.json");
        Type mapStringListOfInteger = new TypeToken<ConcurrentHashMap<String, List<Integer>>>() {}.getType();
        writeJsonMap(postsNetworkPath, SocialNetworkManager.postsNetwork, mapStringListOfInteger);

        String tagsNetworkPath = tags_networkDirPath.concat("tagsNetwork.json");
        writeJsonMap(tagsNetworkPath, SocialNetworkManager.tagsNetwork, mapStringListOfString);

        String removedIDsPath = removed_IDsPath.concat("removedIDs.json");
        Type queueOfInteger = new TypeToken<ConcurrentLinkedQueue<Integer>>() {}.getType();
        writeJsonQueue(removedIDsPath, SocialNetworkManager.removedIDs, queueOfInteger);

    }

    private <K, V> void writeJsonMap(String filepath, Map<K, V> map, Type mapType) {

        try ( Writer writer = new FileWriter(filepath, StandardCharsets.UTF_8) ) {
            JsonWriter jsonWriter = new JsonWriter(writer);
            jsonWriter.setIndent("\n");
            gson.toJson(map, mapType, jsonWriter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private <T> void writeJsonQueue(String filepath, Queue<T> list, Type queueType) {

        try ( Writer writer = new FileWriter(filepath, StandardCharsets.UTF_8) ) {
            JsonWriter jsonWriter = new JsonWriter(writer);
            jsonWriter.setIndent("\n");
            gson.toJson(list, queueType, jsonWriter);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

}