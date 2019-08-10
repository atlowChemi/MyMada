package atlow.chemi.mymada;

class Chat {
    private String time;
    private String sender;
    private String msg;
    private boolean WhatsApp;

    Chat(String sender, String msg, String time, boolean WhatsApp)  {
        this.sender = sender;
        this.msg = msg;
        this.time = time;
        this.WhatsApp = WhatsApp;
    }
    String getSender() {
        return sender;
    }
    void setSender(String sender) {
        this.sender = sender;
    }
    String getMsg() {
        return msg;
    }
    void setMsg(String msg) {
        this.msg = msg;
    }
    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }
    boolean isWhatsApp() {
        return WhatsApp;
    }
    void setWhatsApp(boolean WhatsApp) {
        this.WhatsApp = WhatsApp;
    }
}