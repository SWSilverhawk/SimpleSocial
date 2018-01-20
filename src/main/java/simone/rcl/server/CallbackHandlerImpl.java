package simone.rcl.server;

import simone.rcl.shared.CallbackHandlerInterface;
import simone.rcl.shared.NotifyEventInterface;
import simone.rcl.shared.SimpleSocialMessages;

import java.rmi.RemoteException;

class CallbackHandlerImpl implements CallbackHandlerInterface {

    private ServerOperations serverOperations;

    CallbackHandlerImpl(ServerOperations serverOperations) throws RemoteException {
        this.serverOperations = serverOperations;
    }

    @Override
    public int followUser(NotifyEventInterface clientInterface, String userID, String friendName) throws RemoteException {

        UserData user = serverOperations.checkUserIDValidity(userID);
        UserData friend = serverOperations.getUserByUsername(friendName);

        if (user == null) return SimpleSocialMessages.EXPIRED_USER_ID_INT_ERROR;
        if (friend == null) return SimpleSocialMessages.USER_NOT_EXISTS_ERROR;

        serverOperations.twoUserGetLock(user, friend);

        if (user.getUsername().equals(friendName)) {
            serverOperations.twoUserReleaseLock(user, friend);
            return SimpleSocialMessages.SELF_FOLLOW_ERROR;
        }

        if (!user.getFriendsUsernamesList().contains(friendName)) {
            serverOperations.twoUserReleaseLock(user, friend);
            return SimpleSocialMessages.USER_NOT_FRIEND_ERROR;
        }

        if (user.getFriendsFollowed().contains(friendName)) {
            serverOperations.twoUserReleaseLock(user, friend);
            return SimpleSocialMessages.ALREADY_FOLLOW_ERROR;
        }

        friend.addFriendFollowing(new Follower(clientInterface, user.getUsername()));
        user.addFriendFollowed(friendName);
        serverOperations.saveStatus(user);
        serverOperations.twoUserReleaseLock(user, friend);
        return SimpleSocialMessages.FOLLOW_FRIEND_SUCCESS;

    }

    @Override
    public void registerNewCallback(NotifyEventInterface clientInterface, String userID) throws RemoteException {

        UserData user = serverOperations.getUserByID(userID);

        if (user == null) return;

        user.getLock();

        for (String friendFollowed : user.getFriendsFollowed()) {
            boolean find = false;
            UserData friend = serverOperations.getUserByUsername(friendFollowed);
            for (Follower follower : friend.getFriendsFollowing())
                if (follower.getUsername().equals(user.getUsername())) {
                    follower.setMessageNotifier(clientInterface);
                    find = true;
                    break;
                }
            if (!find) friend.addFriendFollowing(new Follower(clientInterface, user.getUsername()));
        }
        for (BackwardMessage message : user.getBackwardMessages())
            clientInterface.notifyEvent(message.getMessage(), message.getSender());
        user.resetBackwardMessages();
        serverOperations.saveStatus(user);

        user.releaseLock();

    }

    @Override
    public void removeCallback(String userID) throws RemoteException {

        UserData user = serverOperations.getUserByID(userID);

        if (user == null) return;

        user.getLock();

        for (String friendFollowed : user.getFriendsFollowed()) {
            UserData friend = serverOperations.getUserByUsername(friendFollowed);
            for (Follower follower : friend.getFriendsFollowing())
                if (follower.getUsername().equals(user.getUsername())) {
                    follower.setMessageNotifier(null);
                    break;
                }
        }

        user.releaseLock();

    }

    @Override
    public int sendMessage(String message, String userID) throws RemoteException {

        UserData user = serverOperations.checkUserIDValidity(userID);
        if (user == null) return SimpleSocialMessages.EXPIRED_USER_ID_INT_ERROR;

        for (Follower follower : user.getFriendsFollowing()) {

            UserData friend = serverOperations.getUserByUsername(follower.getUsername());
            serverOperations.twoUserGetLock(user, friend);

            if (serverOperations.isOnlineUser(friend))
                follower.getMessageNotifier().notifyEvent(message, user.getUsername());
            else {
                friend.addBackwardMessage(new BackwardMessage(message, user.getUsername()));
                serverOperations.saveStatus(friend);
            }

            serverOperations.twoUserReleaseLock(user, friend);

        }

        return SimpleSocialMessages.OPERATION_COMPLETE;
    }
}
