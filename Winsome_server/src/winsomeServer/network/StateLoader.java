package winsomeServer.network;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import winsome.base.Post;
import winsome.base.User;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class StateLoader {

    private final Gson gson = new Gson();

    private final String usersDirPath;
    private final String usersNetworkDirPath;
    private final String postsDirPath;
    private final String posts_networkDirPath;
    private final String tags_networkDirPath;
    private final String removed_IDsDirPath;

    public StateLoader(String[] paths) {

        if (paths == null)
            throw new NullPointerException();
        if (paths.length != 6)
            throw new IllegalArgumentException();

        usersDirPath = paths[0];
        usersNetworkDirPath = paths[1];
        postsDirPath = paths[2];
        posts_networkDirPath = paths[3];
        tags_networkDirPath = paths[4];
        removed_IDsDirPath = paths[5];

    }

    public void loadState() throws IOException {

        Path dirPath, filePath;

        dirPath = Paths.get(usersDirPath).toAbsolutePath();
        String usersPath = usersDirPath.concat("users.json");
        filePath =Paths.get(usersPath).toAbsolutePath();
        if ( Files.exists(dirPath) && Files.exists(filePath) ) {
            Type mapStringUser = new TypeToken<ConcurrentHashMap<String, User>>() {}.getType();
            SocialNetworkManager.users = readJsonMap(usersPath, mapStringUser);
        }
        else
            SocialNetworkManager.users = new ConcurrentHashMap<>();

        dirPath = Paths.get(usersNetworkDirPath).toAbsolutePath();
        String usersNetworkPath = usersNetworkDirPath.concat("usersNetwork.json");
        filePath = Paths.get(usersNetworkPath).toAbsolutePath();
        if ( Files.exists(dirPath) && Files.exists(filePath) ) {
            Type mapStringListOfString = new TypeToken<ConcurrentHashMap<String, List<String>>>() {}.getType();
            SocialNetworkManager.usersNetwork = readJsonMap(usersNetworkPath, mapStringListOfString);
        }
        else
            SocialNetworkManager.usersNetwork = new ConcurrentHashMap<>();

        dirPath = Paths.get(postsDirPath).toAbsolutePath();
        String postsPath = postsDirPath.concat("posts.json");
        filePath = Paths.get(postsPath).toAbsolutePath();
        if ( Files.exists(dirPath) && Files.exists(filePath) ) {
            Type mapIntegerPost = new TypeToken<ConcurrentHashMap<Integer, Post>>() {}.getType();
            SocialNetworkManager.posts = readJsonMap(postsPath, mapIntegerPost);
        }
        else
            SocialNetworkManager.posts = new ConcurrentHashMap<>();

        dirPath = Paths.get(posts_networkDirPath).toAbsolutePath();
        String postsNetworkPath = posts_networkDirPath.concat("postsNetwork.json");
        filePath = Paths.get(postsNetworkPath).toAbsolutePath();
        if ( Files.exists(dirPath) && Files.exists(filePath) ) {
            Type mapStringListOfInteger = new TypeToken<ConcurrentHashMap<String, List<Integer>>>() {}.getType();
            SocialNetworkManager.postsNetwork = readJsonMap(postsNetworkPath, mapStringListOfInteger);
        }
        else
            SocialNetworkManager.postsNetwork = new ConcurrentHashMap<>();

        dirPath = Paths.get(tags_networkDirPath).toAbsolutePath();
        String tagsNetworkPath = tags_networkDirPath.concat("tagsNetwork.json");
        filePath = Paths.get(tagsNetworkPath).toAbsolutePath();
        if ( Files.exists(dirPath) && Files.exists(filePath) ) {
            Type mapStringListOfString = new TypeToken<ConcurrentHashMap<String, List<String>>>() {}.getType();
            SocialNetworkManager.tagsNetwork = readJsonMap(tagsNetworkPath, mapStringListOfString);
        }
        else
            SocialNetworkManager.tagsNetwork = new ConcurrentHashMap<>();

        dirPath = Paths.get(removed_IDsDirPath).toAbsolutePath();
        String removedIDsPath = removed_IDsDirPath.concat("removeIDs.json");
        filePath = Paths.get(removedIDsPath).toAbsolutePath();
        if ( Files.exists(dirPath) && Files.exists(filePath) ) {
            Type queueOfInteger = new TypeToken<ConcurrentLinkedQueue<Integer>>() {}.getType();
            SocialNetworkManager.removedIDs = readJsonQueue(removedIDsPath, queueOfInteger);
        }
        else
            SocialNetworkManager.removedIDs  = new ConcurrentLinkedQueue<>();

    }

    private <K, V> ConcurrentHashMap<K, V> readJsonMap(String filepath, Type mapType) throws IOException {

        try ( Reader reader = new FileReader(filepath) ) {
            return gson.fromJson(new JsonReader(reader), mapType);
        } catch (IOException e) {
            throw new IOException(e);
        }

    }

    private <T> ConcurrentLinkedQueue<T> readJsonQueue(String filepath, Type queueType) throws IOException {

        try ( Reader reader = new FileReader(filepath) ) {
            return gson.fromJson(new JsonReader(reader), queueType);
        } catch (IOException e) {
            throw new IOException(e);
        }

    }
}