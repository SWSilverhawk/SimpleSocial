package simone.rcl.client;

import simone.rcl.shared.SimpleSocialMessages;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

class FriendRequestReceiver implements Runnable {

    private Frame frame;
    private ServerSocket friendServerSocket;

    FriendRequestReceiver(Frame frame, ServerSocket friendServerSocket){
        this.frame = frame;
        this.friendServerSocket = friendServerSocket;
    }

    @Override
    public void run() {

        String[] options = {"Later", "No", "Yes"};
        JOptionPane friendRequestMessage = new JOptionPane(
                null, JOptionPane.PLAIN_MESSAGE,
                JOptionPane.YES_NO_CANCEL_OPTION, null, options, "Later");
        JDialog friendRequestDialog = friendRequestMessage.createDialog(frame, "New Friend Request");
        friendRequestDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        friendRequestDialog.addComponentListener(new ComponentAdapter() {

            final Timer timer = new Timer(10000, e1 -> friendRequestDialog.setVisible(false));

            @Override
            public void componentShown(ComponentEvent e) {
                super.componentShown(e);
                timer.start();
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                super.componentHidden(e);
                timer.stop();
            }
        });

        try {
            while (!Thread.currentThread().isInterrupted()) {
                Socket friendSocket = friendServerSocket.accept();
                BufferedReader reader = new BufferedReader(new InputStreamReader(friendSocket.getInputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(friendSocket.getOutputStream()));
                int requestsNumber = reader.read();
                for (int i = 0; i < requestsNumber; i++) {
                    String friend = reader.readLine();
                    if (friend == null) {
                        break;
                    }
                    friendRequestMessage.setMessage("New friend request from " + friend + "." + System.lineSeparator() + "Accept?");
                    friendRequestDialog.setLocationRelativeTo(frame);
                    friendRequestDialog.setVisible(true);
                    String result;
                    if (!friendRequestMessage.getValue().equals(JOptionPane.UNINITIALIZED_VALUE))
                        result = (String) friendRequestMessage.getValue();
                    else
                        result = "Later";
                    int requestConfirmation;
                    switch (result) {
                        case "Yes":
                            requestConfirmation = SimpleSocialMessages.REQUEST_ACCEPTED;
                            break;
                        case "No":
                            requestConfirmation = SimpleSocialMessages.REQUEST_REFUSED;
                            break;
                        case "Later":
                            requestConfirmation = SimpleSocialMessages.REQUEST_DELAYED;
                            break;
                        default:
                            requestConfirmation = SimpleSocialMessages.USER_NOT_ONLINE_ERROR;
                            break;
                    }
                    writer.write(requestConfirmation);
                    writer.flush();
                }
                reader.close();
                writer.close();
                friendSocket.close();
            }
        } catch (IOException e) {
            if (!e.getMessage().equals("Socket closed")) e.printStackTrace();
        }
    }
}
