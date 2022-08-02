package winsomeClient.rmi;

import winsome.rmi.ClientNotificationInterface;
import winsome.rmi.ServerInterface;
import winsomeClient.ClientMain;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class ClientRMIManger {

    String username;
    Registry registry;
    ServerInterface server;
    ClientNotificationInterface stub;

    public ClientRMIManger(String username) {

        this.username = username;

        try {
            registry = LocateRegistry.getRegistry(ClientMain.rmiCallbackPort);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        try {
            server = (ServerInterface) registry.lookup(ClientMain.rmiCallbackName);
        } catch (RemoteException | NotBoundException e) {
            throw new RuntimeException(e);
        }

        ClientNotificationInterface callbackObject;
        try {
            callbackObject = new ClientNotificationImplementation();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        try {
            stub = (ClientNotificationInterface) UnicastRemoteObject.exportObject(callbackObject, 0);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

    }

    public void register() {

        try {
            server.registerForCallback(stub, username);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

    }
    public void unregister() {

        try {
            server.unregisterForCallback(stub, username);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

    }

}