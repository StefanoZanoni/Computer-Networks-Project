package winsomeClient.rmi;

import winsome.rmi.ClientNotificationInterface;
import winsome.rmi.ServerInterface;
import winsomeClient.ClientMain;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class ClientRMIManager {

    String username;
    Registry registry;
    ServerInterface server;
    ClientNotificationInterface stub;

    public ClientRMIManager(String username) throws RemoteException, NotBoundException {

        this.username = username;
        registry = LocateRegistry.getRegistry(ClientMain.rmiCallbackPort);
        server = (ServerInterface) registry.lookup(ClientMain.rmiCallbackName);
        ClientNotificationInterface callbackObject = new ClientNotificationImplementation();
        stub = (ClientNotificationInterface) UnicastRemoteObject.exportObject(callbackObject, 0);

    }

    public void register() throws RemoteException { server.registerForCallback(stub, username); }

    public void unregister() {

        try {
            server.unregisterForCallback(stub, username);
        } catch (RemoteException e) {
            e.printStackTrace(System.err);
        }

    }

}