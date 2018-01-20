package simone.rcl.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface NotifyEventInterface extends Remote {

    void notifyEvent(String message, String username) throws RemoteException;

}
