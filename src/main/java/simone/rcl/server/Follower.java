package simone.rcl.server;

import simone.rcl.shared.NotifyEventInterface;

class Follower {

    private NotifyEventInterface messageNotifier;
    private String username;

    Follower(NotifyEventInterface messageNotifier, String username) {
        this.messageNotifier = messageNotifier;
        this.username = username;
    }

    NotifyEventInterface getMessageNotifier() { return messageNotifier; }

    public String getUsername() { return username; }

    void setMessageNotifier(NotifyEventInterface messageNotifier) { this.messageNotifier = messageNotifier; }

    @Override
    public String toString() { return username; }
}
