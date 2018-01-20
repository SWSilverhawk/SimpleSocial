package simone.rcl.server;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import simone.rcl.shared.CallbackHandlerInterface;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MulticastSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocialServer {
    public static void main(String args[]) {

        JSONParser settingsParser = new JSONParser();
        JSONObject settingsFile;

        try {
            settingsFile = (JSONObject) settingsParser.parse(new InputStreamReader(SocialServer.class.getResourceAsStream("/server_settings.json")));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        String keepAliveAddress = (String) settingsFile.get("KeepAliveAddress");
        final long serverPort = (long) settingsFile.get("ServerPort");
        final long registryPort = (long) settingsFile.get("RegistryPort");
        final long userIDPersistence = (long) settingsFile.get("UserIDPersistence");
        final long friendRequestsPersistence = (long) settingsFile.get("FriendRequestsPersistence");

        ServerSocket serverSocket;
        MulticastSocket keepAliveSocket;
        String backupFolder = "data/";
        File folder = new File(backupFolder);
        if (!folder.exists()) if (!folder.mkdir()) return;
        ExecutorService executorService = Executors.newCachedThreadPool();

        ServerOperations serverOperations =
                new ServerOperations(backupFolder, friendRequestsPersistence, userIDPersistence);

        try {

            serverSocket = new ServerSocket((int) serverPort);

            keepAliveSocket = new MulticastSocket(0);

            CallbackHandlerImpl callbackHandler = new CallbackHandlerImpl(serverOperations);
            CallbackHandlerInterface stub =
                    (CallbackHandlerInterface) UnicastRemoteObject.exportObject(callbackHandler, 0);
            Registry registry = LocateRegistry.createRegistry((int) registryPort);

            registry.rebind("SimpleSocial", stub);

        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        executorService.submit(new KeepAliveSignalSender(serverOperations, keepAliveAddress,
                keepAliveSocket));

        while (true) {
            try {
                Socket userChannel = serverSocket.accept();
                executorService.submit(
                        new ClientHandler(userChannel, serverOperations, keepAliveAddress,
                                keepAliveSocket.getLocalPort(), (int) registryPort, userIDPersistence)
                );
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
    }
}
