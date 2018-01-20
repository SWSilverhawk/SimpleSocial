package simone.rcl.client;

import simone.rcl.shared.CallbackHandlerInterface;
import simone.rcl.shared.NotifyEventInterface;
import simone.rcl.shared.SimpleSocialMessages;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

class ClientInterface implements Runnable {

    private static final Dimension defaultButtonDimension = new Dimension(200, 40);
    private static final Color defaultBackground = new Color(32, 71, 176);
    private final long userIDPersistence;
    private String username;
    private String userID;
    private long userIDStart;
    private JFrame frame;
    private JPanel containerPanel;
    private JPanel registerPanel;
    private CardLayout cardLayout;
    private JTextField usernameTextField;
    private JTextField passwordTextField;
    private JTextField postContentTextField;
    private JTextArea contentsTextArea;
    private JLabel usernameErrorLabel;
    private ClientOperations clientOperations;
    private ExecutorService executorService;
    private ServerSocket friendServerSocket;
    private CallbackHandlerInterface callbackHandler;
    private NotifyEventInterface stub;
    private NotifyEventImpl callbackObject;

    ClientInterface(String serverName, int serverPort) {
        clientOperations = new ClientOperations(serverName, serverPort);
        this.userIDPersistence = clientOperations.getUserIDPersistence();

        try {
            Registry registry = LocateRegistry.getRegistry(serverName, clientOperations.getRegistryPort());
            callbackHandler = (CallbackHandlerInterface) registry.lookup("SimpleSocial");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {

        frame = new JFrame("SimpleSocial");

        containerPanel = new JPanel();
        JPanel initialPanel = new JPanel();
        registerPanel = new JPanel();
        JPanel homePanel = new JPanel();

        cardLayout = new CardLayout();

        // Initial Panel
        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Register");
        JPanel loginButtonPanel = new JPanel();
        usernameTextField = new JTextField();
        passwordTextField = new JTextField();

        loginButton.setPreferredSize(defaultButtonDimension);
        registerButton.setPreferredSize(defaultButtonDimension);

        initialPanel.setBackground(defaultBackground);
        loginButtonPanel.setBackground(defaultBackground);

        loginButtonPanel.add(loginButton);
        loginButtonPanel.add(registerButton);

        initialPanel.setLayout(new GridBagLayout());
        initialPanel.add(loginButtonPanel, new GridBagConstraints());

        registerButton.addActionListener(e -> {
            registerPanel.setName("Register");
            cardLayout.show(containerPanel, "registerPanel");
            usernameTextField.requestFocusInWindow();
        });

        loginButton.addActionListener(e -> {
            registerPanel.setName("Login");
            cardLayout.show(containerPanel, "registerPanel");
            usernameTextField.requestFocusInWindow();
        });

        // Register Panel
        JPanel usernamePanel = new JPanel();
        JPanel passwordPanel = new JPanel();
        JPanel usernamePasswordPanel = new JPanel();
        JPanel registerButtonPanel = new JPanel();
        JPanel usernameErrorPanel = new JPanel();
        JLabel usernameLabel = new JLabel("Enter your username: ");
        JLabel passwordLabel = new JLabel("Enter your password: ");
        usernameErrorLabel = new JLabel();
        JButton confirmButton = new JButton("Confirm");
        JButton cancelButton = new JButton("Cancel");

        registerPanel.setLayout(new GridLayout(3, 1));
        usernamePasswordPanel.setLayout(new GridLayout(2, 1));

        usernameTextField.setPreferredSize(new Dimension(250, 20));
        passwordTextField.setPreferredSize(new Dimension(250, 20));
        confirmButton.setPreferredSize(defaultButtonDimension);
        cancelButton.setPreferredSize(defaultButtonDimension);

        registerPanel.setBackground(defaultBackground);
        usernamePanel.setBackground(defaultBackground);
        passwordPanel.setBackground(defaultBackground);
        usernamePasswordPanel.setBackground(defaultBackground);
        registerButtonPanel.setBackground(defaultBackground);
        usernameErrorPanel.setBackground(defaultBackground);

        usernameLabel.setForeground(Color.white);
        passwordLabel.setForeground(Color.white);
        usernameErrorLabel.setForeground(Color.white);

        usernamePanel.add(usernameLabel);
        usernamePanel.add(usernameTextField);
        usernameErrorPanel.add(usernameErrorLabel);
        passwordPanel.add(passwordLabel);
        passwordPanel.add(passwordTextField);
        registerButtonPanel.add(confirmButton);
        registerButtonPanel.add(cancelButton);
        usernamePasswordPanel.add(usernamePanel);
        usernamePasswordPanel.add(passwordPanel);
        registerPanel.add(usernamePasswordPanel);
        registerPanel.add(usernameErrorPanel);
        registerPanel.add(registerButtonPanel);

        usernameTextField.addActionListener(e -> confirmButton.doClick());
        passwordTextField.addActionListener(e -> confirmButton.doClick());

        // Register and Login
        confirmButton.addActionListener(e -> {
            if (registerPanel.getName().equals("Register")) register();
            else login();
        });

        cancelButton.addActionListener(e -> registerAndLoginCancel());

        // Home Panel
        JPanel homePanelButtonsPanel = new JPanel();
        JPanel sendFriendRequestButtonPanel = new JPanel();
        JPanel requestFriendsListButtonPanel = new JPanel();
        JPanel seePendingFriendRequestsButtonPanel = new JPanel();
        JPanel searchUserButtonPanel = new JPanel();
        JPanel followUserButtonPanel = new JPanel();
        JPanel logoutButtonPanel = new JPanel();
        JPanel contentsTextAreaPanel = new JPanel();
        JPanel postContentTextFieldPanel = new JPanel();
        JButton sendFriendRequestButton = new JButton("Send Friend Request");
        JButton requestFriendsListButton = new JButton("Request Friends List");
        JButton seePendingFriendRequestsButton = new JButton("See Pending Friend Requests");
        JButton searchUserButton = new JButton("Search User");
        JButton followUserButton = new JButton("Follow User");
        JButton logoutButton = new JButton("Logout");
        contentsTextArea = new JTextArea(10, 0);
        postContentTextField = new JTextField(34);

        homePanelButtonsPanel.setLayout(new GridLayout(3, 2));
        homePanel.setLayout(new GridLayout(3, 1));

        sendFriendRequestButton.setPreferredSize(defaultButtonDimension);
        requestFriendsListButton.setPreferredSize(defaultButtonDimension);
        seePendingFriendRequestsButton.setPreferredSize(defaultButtonDimension);
        searchUserButton.setPreferredSize(defaultButtonDimension);
        followUserButton.setPreferredSize(defaultButtonDimension);
        logoutButton.setPreferredSize(defaultButtonDimension);

        contentsTextArea.setSize(395, 200);
        contentsTextArea.setBackground(Color.WHITE);
        contentsTextArea.setEditable(false);
        contentsTextArea.setLineWrap(true);

        JScrollPane seeContentsTextAreaScrollPane = new JScrollPane(contentsTextArea);
        seeContentsTextAreaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        homePanel.setBackground(defaultBackground);
        sendFriendRequestButtonPanel.setBackground(defaultBackground);
        requestFriendsListButtonPanel.setBackground(defaultBackground);
        seePendingFriendRequestsButtonPanel.setBackground(defaultBackground);
        searchUserButtonPanel.setBackground(defaultBackground);
        postContentTextFieldPanel.setBackground(defaultBackground);
        followUserButtonPanel.setBackground(defaultBackground);
        logoutButtonPanel.setBackground(defaultBackground);
        contentsTextAreaPanel.setBackground(defaultBackground);

        sendFriendRequestButtonPanel.add(sendFriendRequestButton);
        requestFriendsListButtonPanel.add(requestFriendsListButton);
        seePendingFriendRequestsButtonPanel.add(seePendingFriendRequestsButton);
        searchUserButtonPanel.add(searchUserButton);
        postContentTextFieldPanel.add(postContentTextField);
        followUserButtonPanel.add(followUserButton);
        logoutButtonPanel.add(logoutButton);
        contentsTextAreaPanel.add(seeContentsTextAreaScrollPane);
        homePanelButtonsPanel.add(sendFriendRequestButtonPanel);
        homePanelButtonsPanel.add(requestFriendsListButtonPanel);
        homePanelButtonsPanel.add(seePendingFriendRequestsButtonPanel);
        homePanelButtonsPanel.add(searchUserButtonPanel);
        homePanelButtonsPanel.add(followUserButtonPanel);
        homePanelButtonsPanel.add(logoutButtonPanel);
        homePanel.add(homePanelButtonsPanel);
        homePanel.add(contentsTextAreaPanel);
        homePanel.add(postContentTextFieldPanel);

        // Send Friend Request
        sendFriendRequestButton.addActionListener(e -> sendFriendRequest());

        // Request Friends List
        requestFriendsListButton.addActionListener(e -> requestFriendsList());

        // See Pending Friend Requests
        seePendingFriendRequestsButton.addActionListener(e -> seePendingFriendRequest());

        // Search User
        searchUserButton.addActionListener(e -> searchUser());

        // Post Content
        postContentTextField.addActionListener(e -> postContent());

        // Follow User
        followUserButton.addActionListener(e -> followUser());

        // Logout
        logoutButton.addActionListener(e -> logout());

        // Container
        containerPanel.setLayout(cardLayout);
        containerPanel.add("initialPanel", initialPanel);
        containerPanel.add("registerPanel", registerPanel);
        containerPanel.add("homePanel", homePanel);
        cardLayout.show(containerPanel, "initialPanel");

        // Frame
        frame.add(containerPanel);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

    }

    private void register() {
        username = usernameTextField.getText();
        String password = passwordTextField.getText();
        Pattern notAlphanumericPattern = Pattern.compile("[^a-zA-Z0-9]");
        Pattern alphabeticPattern = Pattern.compile("[a-zA-Z]");
        if (notAlphanumericPattern.matcher(username).find() || !alphabeticPattern.matcher(username).find() ||
                notAlphanumericPattern.matcher(password).find() || !alphabeticPattern.matcher(password).find() ||
                username.equals("") || password.equals("")) {
            usernameErrorLabel.setText("<html><div style='text-align: center;'>Error: invalid username or password.<br><br>" +
                    "Username and password can only contain alphanumeric characters<br><br>" +
                    "and must contain at least one alphabetic character.</html>");
        } else {
            int result = clientOperations.registerUser(username, password);
            if (result == SimpleSocialMessages.OPERATION_COMPLETE) {
                usernameTextField.setText(null);
                passwordTextField.setText(null);
                usernameErrorLabel.setText(null);
                JOptionPane.showMessageDialog(frame, "Registration Complete", "Success", JOptionPane.PLAIN_MESSAGE);
                cardLayout.show(containerPanel, "initialPanel");
            } else {
                usernameErrorLabel.setText("Error: username is already in use");
            }
        }
    }

    private void login() {
        username = usernameTextField.getText();
        String password = passwordTextField.getText();

        try {
            friendServerSocket = new ServerSocket(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String result = clientOperations.login(friendServerSocket.getLocalPort(), username, password);

        switch (result) {

            case SimpleSocialMessages.INVALID_USERNAME_OR_PASSWORD_ERROR:
                usernameErrorLabel.setText("Error: invalid username or password");
                break;

            case SimpleSocialMessages.USER_ALREADY_ONLINE_ERROR:
                usernameErrorLabel.setText("Error: this user is already online");
                break;

            default:
                userID = result;
                userIDStart = System.currentTimeMillis();

                try {
                    callbackObject = new NotifyEventImpl(contentsTextArea);
                    stub = (NotifyEventInterface) UnicastRemoteObject.exportObject(callbackObject, 0);
                    callbackHandler.registerNewCallback(stub, userID);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    return;
                }

                executorService = Executors.newFixedThreadPool(2);
                executorService.submit(
                        new KeepAliveSignalReceiver(userID, clientOperations.getKeepAliveAddress(),
                                clientOperations.getKeepAlivePort())
                );
                executorService.submit(
                        new FriendRequestReceiver(frame, friendServerSocket)
                );

                usernameTextField.setText(null);
                passwordTextField.setText(null);
                usernameErrorLabel.setText(null);

                cardLayout.show(containerPanel, "homePanel");
                break;

        }
    }

    private void registerAndLoginCancel() {
        usernameTextField.setText(null);
        passwordTextField.setText(null);
        usernameErrorLabel.setText(null);
        cardLayout.show(containerPanel, "initialPanel");
    }

    // Send Friend Request
    private void sendFriendRequest() {
        if (!checkUserIDValidity()) return;
        String friend = JOptionPane.showInputDialog(frame, "Insert friend name: ", "Friend Request", JOptionPane.PLAIN_MESSAGE);
        if (friend == null) return;
        if (friend.equals("")) {
            JOptionPane.showMessageDialog(frame, "Insert a valid name", "Friend Request", JOptionPane.PLAIN_MESSAGE);
            return;
        }
        int friendRequestResult = clientOperations.sendFriendRequest(userID, friend);
        switch (friendRequestResult) {
            case SimpleSocialMessages.REQUEST_ACCEPTED:
                JOptionPane.showMessageDialog(frame, "Request Accepted", "Friend Request", JOptionPane.PLAIN_MESSAGE);
                break;
            case SimpleSocialMessages.REQUEST_REFUSED:
                JOptionPane.showMessageDialog(frame, "Request Refused", "Friend Request", JOptionPane.PLAIN_MESSAGE);
                break;
            case SimpleSocialMessages.REQUEST_DELAYED:
                JOptionPane.showMessageDialog(frame, friend + " will answer later to your request", "Friend Request", JOptionPane.PLAIN_MESSAGE);
                break;
            case SimpleSocialMessages.USER_NOT_ONLINE_ERROR:
                JOptionPane.showMessageDialog(frame, "This user not exists or is not online", "Friend Request", JOptionPane.PLAIN_MESSAGE);
                break;
            case SimpleSocialMessages.SELF_REQUEST_ERROR:
                JOptionPane.showMessageDialog(frame, "You can't send friend request to yourself", "Friend Request", JOptionPane.PLAIN_MESSAGE);
                break;
            case SimpleSocialMessages.USER_ALREADY_FRIEND_ERROR:
                JOptionPane.showMessageDialog(frame, friend + " is already your friend.", "Friend Request", JOptionPane.PLAIN_MESSAGE);
                break;
            case SimpleSocialMessages.EXPIRED_USER_ID_INT_ERROR:
                logoutForExpiredUserID();
                break;
        }
    }

    // Request Friends List
    private void requestFriendsList() {
        if (!checkUserIDValidity()) return;
        String friendList = clientOperations.requestFriendList(userID);
        if (friendList.equals(SimpleSocialMessages.EXPIRED_USER_ID_STRING_ERROR + '\n')) {
            logoutForExpiredUserID();
            return;
        }
        if (friendList.equals(""))
            JOptionPane.showMessageDialog(frame, "Your friends list is empty", "Friends List", JOptionPane.PLAIN_MESSAGE);
        else
            JOptionPane.showMessageDialog(frame, friendList, "Friends List", JOptionPane.PLAIN_MESSAGE);
    }

    // See Pending Friend Request
    private void seePendingFriendRequest() {
        if (!checkUserIDValidity()) return;
        int operationResult = clientOperations.seePendingFriendRequest(userID);
        switch (operationResult) {
            case SimpleSocialMessages.NO_FRIEND_REQUESTS:
                JOptionPane.showMessageDialog(frame, "Your have no friend requests to show", "Friends List", JOptionPane.PLAIN_MESSAGE);
                break;
            case SimpleSocialMessages.EXPIRED_USER_ID_INT_ERROR:
                logoutForExpiredUserID();
                break;
            default:
                break;
        }
    }

    // Search User
    private void searchUser() {
        if (!checkUserIDValidity()) return;
        String sequenceToSearch = JOptionPane.showInputDialog(frame, "Insert name to search: ", "Search User", JOptionPane.PLAIN_MESSAGE);
        if (sequenceToSearch == null) return;
        if (sequenceToSearch.equals("")) {
            JOptionPane.showMessageDialog(frame, "Insert a valid name", "Search User", JOptionPane.PLAIN_MESSAGE);
            return;
        }
        String foundUsers = clientOperations.searchUser(userID, sequenceToSearch);
        if (foundUsers.equals(SimpleSocialMessages.EXPIRED_USER_ID_STRING_ERROR + '\n')) {
            logoutForExpiredUserID();
            return;
        }
        if (foundUsers.equals(""))
            JOptionPane.showMessageDialog(frame, "There are no users that match your search", "Found Users", JOptionPane.PLAIN_MESSAGE);
        else
            JOptionPane.showMessageDialog(frame, foundUsers, "Found Users", JOptionPane.PLAIN_MESSAGE);
    }

    // Post Content
    private void postContent() {
        if (!checkUserIDValidity()) return;
        try {
            int operationResult = callbackHandler.sendMessage(postContentTextField.getText(), userID);
            if (operationResult == SimpleSocialMessages.EXPIRED_USER_ID_INT_ERROR) {
                logoutForExpiredUserID();
                return;
            }
            contentsTextArea.append(username + ": " + postContentTextField.getText() + System.lineSeparator());
            postContentTextField.setText(null);
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    // Follow User
    private void followUser() {
        if (!checkUserIDValidity()) return;
        String friend = JOptionPane.showInputDialog(frame, "User to follow: ", "Follow User", JOptionPane.PLAIN_MESSAGE);

        if (friend != null) {
            if (!friend.equals("")) {

                int registrationResult = SimpleSocialMessages.USER_NOT_EXISTS_ERROR;

                try {
                    registrationResult = callbackHandler.followUser(stub, userID, friend);
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }

                switch (registrationResult) {
                    case SimpleSocialMessages.FOLLOW_FRIEND_SUCCESS:
                        JOptionPane.showMessageDialog(frame, "Now you follow " + friend, "Follow User", JOptionPane.PLAIN_MESSAGE);
                        break;
                    case SimpleSocialMessages.USER_NOT_FRIEND_ERROR:
                        JOptionPane.showMessageDialog(frame, friend + " is not your friend", "Follow User", JOptionPane.PLAIN_MESSAGE);
                        break;
                    case SimpleSocialMessages.ALREADY_FOLLOW_ERROR:
                        JOptionPane.showMessageDialog(frame, "You already follow " + friend, "Follow User", JOptionPane.PLAIN_MESSAGE);
                        break;
                    case SimpleSocialMessages.USER_NOT_EXISTS_ERROR:
                        JOptionPane.showMessageDialog(frame, friend + " not exists", "Follow User", JOptionPane.PLAIN_MESSAGE);
                        break;
                    case SimpleSocialMessages.SELF_FOLLOW_ERROR:
                        JOptionPane.showMessageDialog(frame, "You can't follow yourself", "Follow User", JOptionPane.PLAIN_MESSAGE);
                        break;
                    case SimpleSocialMessages.EXPIRED_USER_ID_INT_ERROR:
                        logoutForExpiredUserID();
                        break;
                }

            } else
                JOptionPane.showMessageDialog(frame, "Insert a valid name", "Follow User", JOptionPane.PLAIN_MESSAGE);
        }
    }

    // Logout
    private void logout() {

        try {
            if (friendServerSocket != null) friendServerSocket.close();
            UnicastRemoteObject.unexportObject(callbackObject, true);
            callbackHandler.removeCallback(userID);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        contentsTextArea.setText(null);
        postContentTextField.setText(null);
        executorService.shutdownNow();
        clientOperations.logout(userID);
        cardLayout.show(containerPanel, "initialPanel");

    }

    private void logoutForExpiredUserID() {

        JOptionPane.showMessageDialog(frame, "The current session has expired, please login again", "Session Expired", JOptionPane.PLAIN_MESSAGE);

        try {
            if (friendServerSocket != null) friendServerSocket.close();
            UnicastRemoteObject.unexportObject(callbackObject, true);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        contentsTextArea.setText(null);
        postContentTextField.setText(null);
        executorService.shutdownNow();
        cardLayout.show(containerPanel, "initialPanel");

    }

    private boolean checkUserIDValidity() {
        if (System.currentTimeMillis() - userIDStart > userIDPersistence) {
            JOptionPane.showMessageDialog(frame, "The current session has expired, please login again", "Session Expired", JOptionPane.PLAIN_MESSAGE);
            logout();
            return false;
        }
        return true;
    }

}