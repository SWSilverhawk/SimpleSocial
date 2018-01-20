package simone.rcl.server;

class FriendRequest {

    private String senderUsername;
    private long sendTime;


    FriendRequest(String senderUsername, long sendTime) {
        this.senderUsername = senderUsername;
        this.sendTime = sendTime;
    }

    String getSenderUsername() { return senderUsername; }

    long getSendTime() { return sendTime; }

    @Override
    public String toString() { return "Username: " + senderUsername + " Time: " + sendTime; }
}
