package atlow.chemi.mymada.classes;

public class Chat {
    private String time;
    private String sender;
    private String msg;
    private boolean WhatsApp;

    public Chat(String sender, String msg, String time, boolean WhatsApp)  {
        this.sender = sender;
        this.msg = msg;
        this.time = time;
        this.WhatsApp = WhatsApp;
    }
    public String getSender() {
        return sender;
    }
    public void setSender(String sender) {
        this.sender = sender;
    }
    public String getMsg() {
        return msg;
    }
    public void setMsg(String msg) {
        this.msg = msg;
    }
    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }
    public boolean isWhatsApp() {
        return WhatsApp;
    }
    public void setWhatsApp(boolean WhatsApp) {
        this.WhatsApp = WhatsApp;
    }
}