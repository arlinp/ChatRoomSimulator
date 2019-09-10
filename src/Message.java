public class Message {
    private MessageType type;
    private String sender;
    private String recipient;
    private String message;
    private Long timeStamp;

    public Message(MessageType type, String sender, String recipient, String message, Long timeStamp) {
        this.type = type;
        this.sender = sender;
        this.recipient = recipient;
        this.message = message;
        this.timeStamp = timeStamp;
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

    public Long getTimeStamp() {
        return timeStamp;
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

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }
}
