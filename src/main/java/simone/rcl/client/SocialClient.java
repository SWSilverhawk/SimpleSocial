package simone.rcl.client;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocialClient {
    public static void main(String args[]) {

        JSONParser settingsParser = new JSONParser();
        JSONObject settingsFile;

        try {
            settingsFile = (JSONObject) settingsParser.parse(new FileReader("conf/client_settings.json"));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        String hostname = (String) settingsFile.get("Hostname");
        final long serverPort = (long) settingsFile.get("ServerPort");

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.submit(
                new ClientInterface(hostname, (int) serverPort)
        );
    }
}
