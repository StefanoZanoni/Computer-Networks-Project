package winsomeClient.rmi;

import winsome.rmi.ClientNotificationInterface;
import winsomeClient.ClientMain;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;

public class ClientNotificationImplementation extends RemoteObject implements ClientNotificationInterface {

    public ClientNotificationImplementation() throws RemoteException { super(); }

    @Override
    public void notify(String username, boolean flag) throws RemoteException {

        if (flag) {
            System.out.println(username + " has started following you");
            ClientMain.followers.add(username);
        }
        else {
            System.out.println(username + " has stopped following you");
            ClientMain.followers.remove(username);
        }

    }

}
