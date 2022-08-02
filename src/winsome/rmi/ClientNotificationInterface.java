package winsome.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ClientNotificationInterface extends Remote {

    void notify(String username, boolean flag) throws RemoteException;

}
