package winsomeServer.rmi;

import winsome.rmi.ServerInterface;
import winsomeServer.ServerMain;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class ServerRMIManager {

    ServerImplementation server;
    Registry registry;

    public ServerRMIManager() {

        try {
            server = new ServerImplementation();
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

    }

    public void createRegistry() {

        ServerInterface stub;
        try {
            stub = (ServerInterface) UnicastRemoteObject.exportObject(server, 0);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        try {
            LocateRegistry.createRegistry(ServerMain.rmiCallbackPort);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        try {
            registry = LocateRegistry.getRegistry(ServerMain.rmiCallbackPort);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
        try {
            registry.bind(ServerMain.rmiCallbackName, stub);
        } catch (RemoteException | AlreadyBoundException e) {
            throw new RuntimeException(e);
        }

    }

    public void update(String clientUsername, String user, boolean flag) {
        server.doCallback(clientUsername, user, flag);
    }

    public void unbind() {

        try {
            registry.unbind(ServerMain.rmiCallbackName);
        } catch (RemoteException | NotBoundException e) {
            throw new RuntimeException(e);
        }

    }

}