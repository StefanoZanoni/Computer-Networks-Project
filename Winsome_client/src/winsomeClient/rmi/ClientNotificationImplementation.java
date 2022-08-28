package winsomeClient.rmi;

import winsome.rmi.ClientNotificationInterface;
import winsomeClient.ClientMain;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;

/**
 * This class is used by the server to notify via RMI a new follow-up or un-follow
 */
public class ClientNotificationImplementation extends RemoteObject implements ClientNotificationInterface {

    public ClientNotificationImplementation() throws RemoteException { super(); }

    @Override
    public void notify(String username, boolean flag) throws RemoteException {

        if (flag) {
            System.out.println("< " + username + " has started following you");
            System.out.print("> ");
            ClientMain.followers.add(username);
        }
        else {
            System.out.println("< " + username + " has stopped following you");
            System.out.print("> ");
            ClientMain.followers.remove(username);
        }

    }

}