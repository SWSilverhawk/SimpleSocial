package simone.rcl.client;

import simone.rcl.shared.NotifyEventInterface;

import javax.swing.*;
import java.rmi.RemoteException;

class NotifyEventImpl implements NotifyEventInterface {

    private JTextArea contentsTextArea;

    NotifyEventImpl(JTextArea contentsTextArea) throws RemoteException {
        super();
        this.contentsTextArea = contentsTextArea;
    }

    @Override
    public void notifyEvent(String message, String username) throws RemoteException {
        contentsTextArea.append(username + ": " + message + System.lineSeparator());
    }
}
