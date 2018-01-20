package simone.rcl.server;

class BackwardMessage {

    private final String message;
    private final String sender;

    BackwardMessage(String message, String sender) {
        this.message = message;
        this.sender = sender;
    }

    String getMessage() { return message; }

    String getSender() { return sender; }
}
