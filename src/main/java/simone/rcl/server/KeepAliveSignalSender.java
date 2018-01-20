package simone.rcl.server;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;

class KeepAliveSignalSender implements Runnable {

    private ArrayList<String> onlineUsers;
    private ArrayList<String> previouslyOnlineUsers;
    private ServerOperations serverOperations;
    private String keepAliveAddress;
    private MulticastSocket keepAliveSocket;

    KeepAliveSignalSender(ServerOperations serverOperations, String keepAliveAddress, MulticastSocket keepAliveSocket) {
        this.onlineUsers = new ArrayList<>();
        this.previouslyOnlineUsers = new ArrayList<>();
        this.serverOperations = serverOperations;
        this.keepAliveAddress = keepAliveAddress;
        this.keepAliveSocket = keepAliveSocket;
    }

    @Override
    public void run() {

        try {
            InetAddress keepAliveGroup = InetAddress.getByName(keepAliveAddress);
            DatagramSocket responseSocket = new DatagramSocket(0);
            responseSocket.setSoTimeout(10000);
            byte[] buffer = (Integer.toString(responseSocket.getLocalPort()) + System.lineSeparator()).getBytes();
            byte[] response = new byte[256];
            DatagramPacket responsePacket = new DatagramPacket(response, response.length);

            while (!Thread.currentThread().isInterrupted()) {
                keepAliveSocket.send(new DatagramPacket(buffer, buffer.length, keepAliveGroup, keepAliveSocket.getLocalPort()));
                receiveResponse(responseSocket, responsePacket);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void receiveResponse(DatagramSocket responseSocket, DatagramPacket responsePacket) {

        UserData user;
        previouslyOnlineUsers.clear();
        previouslyOnlineUsers.addAll(onlineUsers);
        onlineUsers.clear();

        while (true) {
            try {
                responseSocket.receive(responsePacket);
                BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(responsePacket.getData())));
                String userID = reader.readLine();
                user = serverOperations.getUserByID(userID);
                if (user != null) onlineUsers.add(user.getUsername());
            } catch (IOException e) {
                break;
            }
        }

        previouslyOnlineUsers.stream().filter(previouslyOnlineUser -> !onlineUsers.contains(previouslyOnlineUser)).
                forEach(previouslyOnlineUser -> {
            UserData notOnlineUser = serverOperations.getUserByUsername(previouslyOnlineUser);
            if (notOnlineUser != null) {
                notOnlineUser.setUserID(null);
                notOnlineUser.setAddress(null);
            }
        });

        System.out.println(onlineUsers.size());

    }

}