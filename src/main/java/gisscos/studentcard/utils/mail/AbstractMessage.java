package gisscos.studentcard.utils.mail;

public class AbstractMessage {
    public String toAddr;
    public String messageText;
    public String errorText;

    public AbstractMessage() {
    }

    public AbstractMessage(String toAddr) {
        this.toAddr = toAddr;
    }

    public AbstractMessage(String toAddr, String messageText, String errorText) {
        this.toAddr = toAddr;
        this.messageText = messageText;
        this.errorText = errorText;
    }

    public String getToAddr() {
        return toAddr;
    }

    public void setToAddr(String toAddr) {
        this.toAddr = toAddr;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getErrorText() {
        return errorText;
    }

    public void setErrorText(String errorText) {
        this.errorText = errorText;
    }
}
