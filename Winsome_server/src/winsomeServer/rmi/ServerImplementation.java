package winsomeServer.rmi;

import winsome.rmi.ClientNotificationInterface;
import winsome.rmi.ServerInterface;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.*;

public class ServerImplementation extends RemoteObject implements ServerInterface {

    private final List<ClientNotificationInterface> clients;
    private final Map<String, ClientNotificationInterface> map;

    public ServerImplementation() throws RemoteException {

        clients = new ArrayList<>();
        map = new HashMap<>();

    }

    @Override
    public synchronized void registerForCallback(ClientNotificationInterface client, String username)
            throws RemoteException {

        if (!clients.contains(client))
            clients.add(client);
        map.putIfAbsent(username, client);

    }

    @Override
    public synchronized void unregisterForCallback(ClientNotificationInterface client, String username)
            throws RemoteException {

        clients.remove(client);
        map.remove(username, client);

    }

    public void doCallback(String clientUsername, String user, boolean flag) {

        ClientNotificationInterface client = map.get(clientUsername);
        if (client != null)
            try {
                client.notify(user, flag);
            } catch (RemoteException e) {
                e.printStackTrace(System.err);
            }

    }

}