package simone.rcl.server;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import simone.rcl.shared.SimpleSocialMessages;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

class ServerOperations {

    private ConcurrentLinkedQueue<UserData> registeredUsers;
    private long userProgressiveNumber;
    private ReentrantLock registerLock;
    private final String backupFolder;
    private final long friendRequestPersistence;
    private final long userIDPersistence;

    ServerOperations(String backupFolder, long friendRequestsPersistence, long userIDPersistence) {
        registeredUsers = new ConcurrentLinkedQueue<>();
        this.userProgressiveNumber = 0;
        this.registerLock = new ReentrantLock();
        this.backupFolder = backupFolder;
        this.friendRequestPersistence = friendRequestsPersistence;
        this.userIDPersistence = userIDPersistence;
        restoreStatus();
    }

    int registerUser(String username, String password) {
        registerLock.lock();
        if (getUserByUsername(username) == null) {
            UserData user = new UserData(username, password, userProgressiveNumber);
            userProgressiveNumber++;
            registeredUsers.add(user);
            saveStatus(user);
            registerLock.unlock();
            return SimpleSocialMessages.OPERATION_COMPLETE;
        } else {
            registerLock.unlock();
            return SimpleSocialMessages.USERNAME_ALREADY_IN_USE_ERROR;
        }
    }

    String login(String username, String password, InetAddress address, int friendRequestPort) {

        UserData user = getUserByUsername(username);

        if (user == null || !password.equals(user.getPassword())) return SimpleSocialMessages.INVALID_USERNAME_OR_PASSWORD_ERROR;

        user.getLock();

        if (isOnlineUser(user)) {
            user.releaseLock();
            return SimpleSocialMessages.USER_ALREADY_ONLINE_ERROR;
        }
        String userID = new UID().toString();
        user.setUserID(userID);
        user.setUserIDStart(System.currentTimeMillis());
        user.setAddress(address);
        user.setFriendRequestPort(friendRequestPort);

        user.releaseLock();

        return userID;

    }

    int sendFriendRequest(String userID, String friendName) {

        UserData requestSender = checkUserIDValidity(userID);
        UserData requestReceiver = getUserByUsername(friendName);

        if (requestSender == null) return SimpleSocialMessages.EXPIRED_USER_ID_INT_ERROR;
        if (requestReceiver == null) return SimpleSocialMessages.USER_NOT_ONLINE_ERROR;

        twoUserGetLock(requestSender, requestReceiver);

        if (requestSender.getUsername().equals(friendName)) {
            twoUserReleaseLock(requestSender, requestReceiver);
            return SimpleSocialMessages.SELF_REQUEST_ERROR;
        }

        if (requestSender.getFriendsUsernamesList().contains(friendName)) {
            twoUserReleaseLock(requestSender, requestReceiver);
            return SimpleSocialMessages.USER_ALREADY_FRIEND_ERROR;
        }

        if (requestReceiver.getAddress() != null) {

            try {

                Socket friendSocket = new Socket(requestReceiver.getAddress(), requestReceiver.getFriendRequestPort());
                BufferedReader reader = new BufferedReader(new InputStreamReader(friendSocket.getInputStream()));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(friendSocket.getOutputStream()));
                writer.write(1);
                writer.write(requestSender.getUsername());
                writer.newLine();
                writer.flush();
                int requestResult = reader.read();

                if (requestResult == SimpleSocialMessages.REQUEST_ACCEPTED) {
                    requestReceiver.addFriend(requestSender);
                    requestReceiver.addFriendUsername(requestSender.getUsername());
                    requestSender.addFriend(requestReceiver);
                    requestSender.addFriendUsername(friendName);
                    saveStatus(requestSender);
                    saveStatus(requestReceiver);
                } else if (requestResult == SimpleSocialMessages.REQUEST_DELAYED) {
                    requestReceiver.addFriendRequest(new FriendRequest(requestSender.getUsername(), System.currentTimeMillis()));
                }

                reader.close();
                writer.close();
                friendSocket.close();

                twoUserReleaseLock(requestSender, requestReceiver);
                return requestResult;

            } catch (IOException e) {
                twoUserReleaseLock(requestSender, requestReceiver);
                return SimpleSocialMessages.USER_NOT_ONLINE_ERROR;
            }
        }

        twoUserReleaseLock(requestSender, requestReceiver);
        return SimpleSocialMessages.USER_NOT_ONLINE_ERROR;
    }

    ArrayList<UserData> requestFriendList(String userID) {

        UserData user = checkUserIDValidity(userID);
        if (user == null) return null;
        return user.getFriendsList();

    }

    void seePendingFriendRequest(String userID, BufferedWriter clientWriter) {

        UserData requestReceiver = checkUserIDValidity(userID);

        try {

            if (requestReceiver == null) {
                clientWriter.write(SimpleSocialMessages.EXPIRED_USER_ID_INT_ERROR);
                clientWriter.flush();
                return;
            }

            if (requestReceiver.getPendingFriendRequests().size() == 0) {
                clientWriter.write(SimpleSocialMessages.NO_FRIEND_REQUESTS);
                clientWriter.flush();
                return;
            }

            requestReceiver.getLock();

            clientWriter.write(SimpleSocialMessages.OPERATION_COMPLETE);
            clientWriter.flush();
            Socket friendSocket = new Socket(requestReceiver.getAddress(), requestReceiver.getFriendRequestPort());
            BufferedReader reader = new BufferedReader(new InputStreamReader(friendSocket.getInputStream()));
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(friendSocket.getOutputStream()));
            ArrayList<FriendRequest> pendingRequests = new ArrayList<>(requestReceiver.getPendingFriendRequests());
            requestReceiver.resetPendingFriendRequestsList();
            writer.write(pendingRequests.size());
            writer.flush();

            requestReceiver.releaseLock();

            for (FriendRequest request : pendingRequests) {

                UserData requestSender = getUserByUsername(request.getSenderUsername());

                twoUserGetLock(requestSender, requestReceiver);

                if ((System.currentTimeMillis() - friendRequestPersistence) < request.getSendTime() &&
                        !requestReceiver.getFriendsUsernamesList().contains(requestSender.getUsername())) {
                    writer.write(requestSender.getUsername());
                    writer.newLine();
                    writer.flush();
                    int requestResult = reader.read();

                    if (requestResult == SimpleSocialMessages.REQUEST_ACCEPTED) {

                        requestReceiver.addFriend(requestSender);
                        requestReceiver.addFriendUsername(requestSender.getUsername());
                        requestSender.addFriend(requestReceiver);
                        requestSender.addFriendUsername(requestReceiver.getUsername());
                        saveStatus(requestSender);

                    } else if (requestResult == SimpleSocialMessages.REQUEST_DELAYED) {
                        requestReceiver.addFriendRequest(request);
                    }

                }

                twoUserReleaseLock(requestSender, requestReceiver);

            }

            requestReceiver.getLock();

            saveStatus(requestReceiver);
            reader.close();
            writer.close();
            friendSocket.close();

            requestReceiver.releaseLock();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    ArrayList<String> searchUser(String userID, String sequenceToSearch) {

        UserData user = checkUserIDValidity(userID);
        if (user == null) return null;
        ArrayList<String> foundUsers = new ArrayList<>();
        String username;

        for (UserData searchedUser : registeredUsers) {
            username = searchedUser.getUsername();
            if (username.contains(sequenceToSearch))
                foundUsers.add(username);
        }

        return foundUsers;
    }

    void logout(String userID) {

        UserData user = checkUserIDValidity(userID);

        if (user != null) {
            user.setUserID(null);
            user.setAddress(null);
        }

    }

    UserData getUserByID(String userID) {
        for (UserData user : registeredUsers) {
            if (userID.equals(user.getUserID()))
                return user;
        }
        return null;
    }

    UserData getUserByUsername(String username) {
        for (UserData user : registeredUsers) {
            if (username.equals(user.getUsername()))
                return user;
        }
        return null;
    }

    boolean isOnlineUser(UserData user) {
        if (user.getAddress() == null) return false;
        try {
            Socket onlineCheckSocket = new Socket(user.getAddress(), user.getFriendRequestPort());
            onlineCheckSocket.close();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    UserData checkUserIDValidity(String userID) {
        UserData user = getUserByID(userID);
        if (user == null) return null;
        if (System.currentTimeMillis() - user.getUserIDStart() > userIDPersistence) {
            user.setUserID(null);
            user.setAddress(null);
            return null;
        }
        return user;
    }

    void twoUserGetLock(UserData firstUser, UserData secondUser) {

        if (firstUser.getUserProgressiveNumber() < secondUser.getUserProgressiveNumber()) {
            firstUser.getLock();
            secondUser.getLock();
        } else {
            secondUser.getLock();
            firstUser.getLock();
        }

    }

    void twoUserReleaseLock(UserData firstUser, UserData secondUser) {

        if (firstUser.getUserProgressiveNumber() < secondUser.getUserProgressiveNumber()) {
            secondUser.releaseLock();
            firstUser.releaseLock();
        } else {
            firstUser.releaseLock();
            secondUser.releaseLock();
        }

    }

    @SuppressWarnings("unchecked")
    void saveStatus(UserData user) {

        JSONObject userObject = new JSONObject();
        userObject.put("Username", user.getUsername());
        userObject.put("Password", user.getPassword());

        JSONArray friends = new JSONArray();
        friends.addAll(user.getFriendsUsernamesList());
        userObject.put("Friends", friends);

        JSONArray friendsFollowed = new JSONArray();
        friendsFollowed.addAll(user.getFriendsFollowed());
        userObject.put("Friends Followed", friendsFollowed);

        JSONArray backwardMessages = new JSONArray();
        for (BackwardMessage message : user.getBackwardMessages()) {
            JSONObject backwardMessage = new JSONObject();
            backwardMessage.put("Message", message.getMessage());
            backwardMessage.put("Sender", message.getSender());
            backwardMessages.add(backwardMessage);
        }
        userObject.put("Backward Messages", backwardMessages);

        try (FileWriter file = new FileWriter(backupFolder + user.getUsername() + ".json")) {
            file.write(userObject.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void restoreStatus() {
        File folder = new File(backupFolder);
        File[] filesList = folder.listFiles();
        String username;
        String password;
        String sender;
        String message;
        if (filesList != null) {
            try {
                for (File userDataFile : filesList) {
                    if (!userDataFile.getName().contains(".json")) continue;
                    JSONParser parser = new JSONParser();
                    JSONObject userObject = (JSONObject) parser.parse(new FileReader(userDataFile));

                    username = (String) userObject.get("Username");
                    password = (String) userObject.get("Password");

                    UserData user = new UserData(username, password, userProgressiveNumber);
                    userProgressiveNumber++;

                    JSONArray friends = (JSONArray) userObject.get("Friends");
                    for (Object friend : friends) user.addFriendUsername(friend.toString());

                    JSONArray friendsFollowed = (JSONArray) userObject.get("Friends Followed");
                    for (Object friend : friendsFollowed) user.addFriendFollowed(friend.toString());

                    JSONArray backwardMessages = (JSONArray) userObject.get("Backward Messages");
                    for (Object backwardMessage : backwardMessages) {
                        JSONObject messageObject = (JSONObject) backwardMessage;
                        message = (String) messageObject.get("Message");
                        sender = (String) messageObject.get("Sender");
                        user.addBackwardMessage(new BackwardMessage(message, sender));
                    }

                    registeredUsers.add(user);
                }

                for (UserData registeredUser : registeredUsers) {
                    for (String friendName : registeredUser.getFriendsUsernamesList()) {
                        registeredUser.addFriend(getUserByUsername(friendName));
                    }
                    for (String followName : registeredUser.getFriendsFollowed()) {
                        getUserByUsername(followName).addFriendFollowing(new Follower(null, registeredUser.getUsername()));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
