import java.util.ArrayList;

public class Message {
    private MessageType type;
    private String sender;
    private String recipient;
    private String message;
    private long timeSent;
    private long timeReceived;
    private ArrayList<String> users;

    public Message(MessageType type){
        this.type = type;
    }

    public Message(MessageType type, String sender) {
        this.type = type;
        this.sender = sender;
    }

    public Message(MessageType type, String sender, String recipient, String message, Long timeStamp) {
        this.type = type;
        this.sender = sender;
        this.recipient = recipient;
        this.message = message;
        this.timeSent = timeStamp;
    }

    public Message(MessageType type, ArrayList<String> users) {
        this.type = type;
        this.users = users;
    }

    public ArrayList<String> getUsers() {
        return users;
    }

    public MessageType getType() {
        return type;
    }

    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getMessage() {
        return message;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTimeSent(long timeSent) {
        this.timeSent = timeSent;
    }

    public long getTimeSent() {
        return timeSent;
    }

    public long getTimeReceived() {
        return timeReceived;
    }

    public void setTimeReceived(long timeReceived) {
        this.timeReceived = timeReceived;
    }
}
