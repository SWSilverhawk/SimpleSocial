package simone.rcl.client;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;

class KeepAliveSignalReceiver implements Runnable {

    private String keepAliveAddress;
    private int keepAlivePort;
    private String userID;

    KeepAliveSignalReceiver(String userID, String keepAliveAddress, int keepAlivePort) {
        this.userID = userID;
        this.keepAliveAddress = keepAliveAddress;
        this.keepAlivePort = keepAlivePort;
    }

    @Override
    public void run() {

        try {
            InetAddress keepAliveGroup = InetAddress.getByName(keepAliveAddress);
            MulticastSocket keepAliveSocket = new MulticastSocket(keepAlivePort);
            DatagramSocket responseSocket = new DatagramSocket();
            keepAliveSocket.joinGroup(keepAliveGroup);
            DatagramPacket keepAliveMessage;
            byte[] buffer = new byte[256];
            byte[] response = (userID + System.lineSeparator()).getBytes();

            while (!Thread.currentThread().isInterrupted()) {
                keepAliveMessage = new DatagramPacket(buffer, buffer.length);
                keepAliveSocket.receive(keepAliveMessage);
                BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(keepAliveMessage.getData())));
                int responsePort = Integer.parseInt(reader.readLine());

                responseSocket.send(new DatagramPacket(response, response.length, keepAliveMessage.getAddress(), responsePort));
            }

            keepAliveSocket.leaveGroup(keepAliveGroup);
            keepAliveSocket.close();
            responseSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
