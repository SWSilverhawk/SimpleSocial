package simone.rcl.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface CallbackHandlerInterface extends Remote {

    int followUser(NotifyEventInterface clientInterface, String userID, String friendName) throws RemoteException;

    void registerNewCallback(NotifyEventInterface clientInterface, String userID) throws RemoteException;

    void removeCallback(String userID) throws RemoteException;

    int sendMessage(String message, String userID) throws RemoteException;

}
