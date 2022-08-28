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

    public ServerRMIManager() throws RemoteException { server = new ServerImplementation(); }

    public void createRegistry() throws RemoteException, AlreadyBoundException {

        ServerInterface stub;
        stub = (ServerInterface) UnicastRemoteObject.exportObject(server, 0);
        LocateRegistry.createRegistry(ServerMain.rmiCallbackPort);
        registry = LocateRegistry.getRegistry(ServerMain.rmiCallbackPort);
        registry.bind(ServerMain.rmiCallbackName, stub);

    }

    public void update(String clientUsername, String user, boolean flag) {

        server.doCallback(clientUsername, user, flag);

    }

    public void unbind() {

        try {
            registry.unbind(ServerMain.rmiCallbackName);
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace(System.err);
        }

    }

}