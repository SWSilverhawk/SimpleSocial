package simone.rcl.server;

import simone.rcl.shared.SimpleSocialMessages;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

class ClientHandler implements Runnable {

    private Socket userChannel;
    private ServerOperations serverOperations;
    private final String keepAliveAddress;
    private final int keepAlivePort;
    private final int registryPort;
    private final long userIDPersistence;

    ClientHandler(Socket userChannel, ServerOperations serverOperations, String keepAliveAddress,
                  int keepAlivePort, int registryPort, long userIDPersistence) {
        this.userChannel = userChannel;
        this.serverOperations = serverOperations;
        this.keepAliveAddress = keepAliveAddress;
        this.keepAlivePort = keepAlivePort;
        this.registryPort = registryPort;
        this.userIDPersistence = userIDPersistence;
    }

    @Override
    public void run() {

        BufferedReader reader;
        BufferedWriter writer;
        int operationCode;
        String username;
        String password;
        String userID;

        try {
            reader = new BufferedReader(new InputStreamReader(userChannel.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(userChannel.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        try {
            operationCode = reader.read();
            switch (operationCode) {

                // Get Server Information
                case SimpleSocialMessages.GET_SERVER_INFORMATION:
                    writer.write(keepAliveAddress);
                    writer.newLine();
                    writer.write(Integer.toString(keepAlivePort));
                    writer.newLine();
                    writer.write(Integer.toString(registryPort));
                    writer.newLine();
                    writer.write(Long.toString(userIDPersistence));
                    writer.newLine();
                    writer.flush();
                    break;

                // Register User
                case SimpleSocialMessages.REGISTER_USER:
                    username = reader.readLine();
                    password = reader.readLine();
                    writer.write(serverOperations.registerUser(username, password));
                    writer.flush();
                    break;

                // Login
                case SimpleSocialMessages.LOGIN_USER:
                    int friendRequestPort = Integer.parseInt(reader.readLine());
                    username = reader.readLine();
                    password = reader.readLine();
                    userID = serverOperations.login(username, password, userChannel.getInetAddress(), friendRequestPort);
                    writer.write(userID);
                    writer.newLine();
                    writer.flush();
                    break;

                // Send Friend Request
                case SimpleSocialMessages.SEND_FRIEND_REQUEST:
                    userID = reader.readLine();
                    String friendName = reader.readLine();
                    int requestResult = serverOperations.sendFriendRequest(userID, friendName);
                    writer.write(requestResult);
                    writer.flush();
                    break;

                // Request Friends List
                case SimpleSocialMessages.REQUEST_FRIEND_LIST:
                    userID = reader.readLine();
                    ArrayList<UserData> friendsList = serverOperations.requestFriendList(userID);
                    if (friendsList == null) {
                        writer.write(1);
                        writer.write(SimpleSocialMessages.EXPIRED_USER_ID_STRING_ERROR);
                        writer.newLine();
                    } else {
                        writer.write(friendsList.size());
                        for (UserData user : friendsList) {
                            if (user.getAddress() != null)
                                writer.write("Friend Name: " + user.getUsername() + "  Status: Online");
                            else
                                writer.write("Friend Name: " + user.getUsername() + "  Status: Offline");
                            writer.newLine();
                        }
                    }
                    writer.flush();
                    break;

                // See Pending Friend Request
                case SimpleSocialMessages.SEE_PENDING_FRIEND_REQUEST:
                    userID = reader.readLine();
                    serverOperations.seePendingFriendRequest(userID, writer);
                    break;

                // Search User
                case SimpleSocialMessages.SEARCH_USER:
                    userID = reader.readLine();
                    String sequenceToSearch = reader.readLine();
                    ArrayList<String> foundUsers = serverOperations.searchUser(userID, sequenceToSearch);
                    if (foundUsers == null) {
                        writer.write(1);
                        writer.write(SimpleSocialMessages.EXPIRED_USER_ID_STRING_ERROR);
                        writer.newLine();
                    } else {
                        writer.write(foundUsers.size());
                        for (String foundUserName : foundUsers) {
                            writer.write(foundUserName);
                            writer.newLine();
                        }
                    }
                    writer.flush();
                    break;

                // Logout
                case SimpleSocialMessages.LOGOUT_USER:
                    userID = reader.readLine();
                    serverOperations.logout(userID);
                    break;
            }
            reader.close();
            writer.close();
            userChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
