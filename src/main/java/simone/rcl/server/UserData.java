package simone.rcl.server;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

class UserData {

    private final String username;
    private final String password;
    private final long userProgressiveNumber;
    private String userID;
    private long userIDStart;
    private InetAddress address;
    private int friendRequestPort;
    private ReentrantLock userLock;
    private ArrayList<UserData> friendsList;
    private ArrayList<String> friendsUsernamesList;
    private ArrayList<FriendRequest> pendingFriendRequests;
    private ArrayList<String> friendsFollowed;
    private ArrayList<Follower> friendsFollowing;
    private ArrayList<BackwardMessage> backwardMessages;

    UserData(String username, String password, long userProgressiveNumber) {
        this.username = username;
        this.password = password;
        this.userProgressiveNumber = userProgressiveNumber;
        userLock = new ReentrantLock();
        friendsList = new ArrayList<>();
        friendsUsernamesList = new ArrayList<>();
        friendsFollowed = new ArrayList<>();
        friendsFollowing = new ArrayList<>();
        pendingFriendRequests = new ArrayList<>();
        backwardMessages = new ArrayList<>();
    }

    String getUsername() { return username; }

    String getPassword() { return password; }

    long getUserProgressiveNumber() { return userProgressiveNumber; }

    String getUserID() { return userID; }

    long getUserIDStart() { return userIDStart; }

    InetAddress getAddress() { return address; }

    int getFriendRequestPort() { return  friendRequestPort; }

    ArrayList<UserData> getFriendsList() { return friendsList; }

    ArrayList<String> getFriendsUsernamesList() { return friendsUsernamesList; }

    ArrayList<FriendRequest> getPendingFriendRequests() { return pendingFriendRequests; }

    ArrayList<String> getFriendsFollowed() { return friendsFollowed; }

    ArrayList<Follower> getFriendsFollowing() { return friendsFollowing; }

    ArrayList<BackwardMessage> getBackwardMessages() { return backwardMessages; }

    void setUserID(String userID) { this.userID = userID; }

    void setUserIDStart(long userIDStart) { this.userIDStart = userIDStart; }

    void setAddress(InetAddress address) { this.address = address; }

    void setFriendRequestPort(int friendRequestPort) { this.friendRequestPort = friendRequestPort; }

    void addFriend(UserData user) { friendsList.add(user); }

    void addFriendUsername(String username) { friendsUsernamesList.add(username); }

    void addFriendRequest(FriendRequest username) { pendingFriendRequests.add(username); }

    void addFriendFollowed(String friend) { friendsFollowed.add(friend); }

    void addFriendFollowing(Follower friend) { friendsFollowing.add(friend); }

    void addBackwardMessage(BackwardMessage message) { backwardMessages.add(message); }

    void resetPendingFriendRequestsList() { pendingFriendRequests.clear(); }

    void resetBackwardMessages() { backwardMessages.clear(); }

    void getLock() { userLock.lock(); }

    void releaseLock() { userLock.unlock(); }

    @Override
    public String toString() { return "Username: " + username + ", Password: " + password; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserData that = (UserData) o;
        return username.equals(that.username) && password.equals(that.password);
    }

    @Override
    public int hashCode() {
        int result = username.hashCode();
        result = 31 * result + password.hashCode();
        return result;
    }

}
