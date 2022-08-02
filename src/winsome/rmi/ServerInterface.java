package winsome.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote {

    void registerForCallback(ClientNotificationInterface stub, String username) throws RemoteException;
    void unregisterForCallback(ClientNotificationInterface stub, String username) throws RemoteException;


}
