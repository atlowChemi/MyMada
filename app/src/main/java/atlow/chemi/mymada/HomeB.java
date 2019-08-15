package atlow.chemi.mymada;

public class HomeB {
    private String intent;
    private int bg;
    HomeB() {
    }
    public HomeB(String i, int img) {
        this.intent = i;
        this.bg = img;
    }
    public String getIntent() {
        return this.intent;
    }
    public void setIntent(String i)    {
        this.intent = i;
    }
    public int getBg()  {
        return this.bg;
    }
    public void setBg(int img)  {
        this.bg = img;
    }
}
