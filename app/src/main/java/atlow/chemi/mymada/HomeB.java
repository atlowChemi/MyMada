package atlow.chemi.mymada;

class HomeB {
    private String intent;
    private int bg;
    HomeB() {
    }
    HomeB(String i, int img) {
        this.intent = i;
        this.bg = img;
    }
    String getIntent() {
        return this.intent;
    }
    void setIntent(String i)    {
        this.intent = i;
    }
    int getBg()  {
        return this.bg;
    }
    void setBg(int img)  {
        this.bg = img;
    }
}
