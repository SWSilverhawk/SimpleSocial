package simone.rcl.client;

import simone.rcl.shared.SimpleSocialMessages;

import java.io.*;
import java.net.Socket;

class ClientOperations {

    private Socket serverChannel;
    private BufferedReader reader;
    private BufferedWriter writer;
    private final String serverName;
    private final int serverPort;
    private String keepAliveAddress;
    private int keepAlivePort;
    private int registryPort;
    private long userIDPersistence;

    ClientOperations(String serverName, int serverPort) {

        this.serverName = serverName;
        this.serverPort = serverPort;

        try {

            serverChannel = new Socket(serverName, serverPort);
            reader = new BufferedReader(new InputStreamReader(serverChannel.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(serverChannel.getOutputStream()));
            writer.write(SimpleSocialMessages.GET_SERVER_INFORMATION);
            writer.flush();
            keepAliveAddress = reader.readLine();
            keepAlivePort = Integer.parseInt(reader.readLine());
            registryPort = Integer.parseInt(reader.readLine());
            userIDPersistence = Long.parseLong(reader.readLine());
            reader.close();
            writer.close();
            serverChannel.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    int registerUser(String username, String password) {

        try {
            serverChannel = new Socket(serverName, serverPort);
            reader = new BufferedReader(new InputStreamReader(serverChannel.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(serverChannel.getOutputStream()));
            writer.write(SimpleSocialMessages.REGISTER_USER);
            writer.write(username);
            writer.newLine();
            writer.write(password);
            writer.newLine();
            writer.flush();
            int result = reader.read();
            reader.close();
            writer.close();
            serverChannel.close();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return SimpleSocialMessages.USERNAME_ALREADY_IN_USE_ERROR;
        }

    }

    String login(int friendRequestPort, String username, String password) {

        try {
            serverChannel = new Socket(serverName, serverPort);
            reader = new BufferedReader(new InputStreamReader(serverChannel.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(serverChannel.getOutputStream()));
            writer.write(SimpleSocialMessages.LOGIN_USER);
            writer.write(Integer.toString(friendRequestPort));
            writer.newLine();
            writer.write(username);
            writer.newLine();
            writer.write(password);
            writer.newLine();
            writer.flush();
            String result = reader.readLine();
            reader.close();
            writer.close();
            serverChannel.close();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return SimpleSocialMessages.INVALID_USERNAME_OR_PASSWORD_ERROR;
        }

    }

    int sendFriendRequest(String userID, String friendName) {

        try {
            serverChannel = new Socket(serverName, serverPort);
            reader = new BufferedReader(new InputStreamReader(serverChannel.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(serverChannel.getOutputStream()));
            writer.write(SimpleSocialMessages.SEND_FRIEND_REQUEST);
            writer.write(userID);
            writer.newLine();
            writer.write(friendName);
            writer.newLine();
            writer.flush();
            int requestResult = reader.read();
            reader.close();
            writer.close();
            serverChannel.close();
            return requestResult;
        } catch (IOException e) {
            e.printStackTrace();
            return SimpleSocialMessages.USER_NOT_ONLINE_ERROR;
        }

    }

    String requestFriendList(String userID) {

        try {
            serverChannel = new Socket(serverName, serverPort);
            reader = new BufferedReader(new InputStreamReader(serverChannel.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(serverChannel.getOutputStream()));
            writer.write(SimpleSocialMessages.REQUEST_FRIEND_LIST);
            writer.write(userID);
            writer.newLine();
            writer.flush();
            StringBuilder friendsList = new StringBuilder();
            int friendsNumber = reader.read();
            for (int i = 0; i < friendsNumber; i++)
                friendsList.append(reader.readLine()).append(System.lineSeparator());
            reader.close();
            writer.close();
            serverChannel.close();
            return friendsList.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

    }

    String searchUser(String userID, String sequenceToSearch) {

        try {
            serverChannel = new Socket(serverName, serverPort);
            reader = new BufferedReader(new InputStreamReader(serverChannel.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(serverChannel.getOutputStream()));
            writer.write(SimpleSocialMessages.SEARCH_USER);
            writer.write(userID);
            writer.newLine();
            writer.write(sequenceToSearch);
            writer.newLine();
            writer.flush();
            StringBuilder foundUserList = new StringBuilder();
            int foundUserNumber = reader.read();
            for (int i = 0; i < foundUserNumber; i++)
                foundUserList.append(reader.readLine()).append(System.lineSeparator());
            reader.close();
            writer.close();
            serverChannel.close();
            return foundUserList.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    int seePendingFriendRequest(String userID) {

        try {
            serverChannel = new Socket(serverName, serverPort);
            reader = new BufferedReader(new InputStreamReader(serverChannel.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(serverChannel.getOutputStream()));
            writer.write(SimpleSocialMessages.SEE_PENDING_FRIEND_REQUEST);
            writer.write(userID);
            writer.newLine();
            writer.flush();
            int operationResult = reader.read();
            reader.close();
            writer.close();
            serverChannel.close();
            return operationResult;
        } catch (IOException e) {
            e.printStackTrace();
            return SimpleSocialMessages.NO_FRIEND_REQUESTS;
        }
    }

    void logout(String userID) {

        try {
            serverChannel = new Socket(serverName, serverPort);
            reader = new BufferedReader(new InputStreamReader(serverChannel.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(serverChannel.getOutputStream()));
            writer.write(SimpleSocialMessages.LOGOUT_USER);
            writer.write(userID);
            writer.newLine();
            writer.flush();
            reader.close();
            writer.close();
            serverChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    String getKeepAliveAddress() { return  keepAliveAddress; }

    int getKeepAlivePort() { return keepAlivePort; }

    int getRegistryPort() { return  registryPort; }

    long getUserIDPersistence() { return userIDPersistence; }
}
