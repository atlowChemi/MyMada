package atlow.chemi.mymada.classes;

public class mCodeKid {
    public String id;
    private String name, catogory;
    private int order;
    private boolean isChecked;
    public mCodeKid(String name, String id, String catogory, int order) {
        this.id = id;
        this.name = name;
        this.catogory = catogory;
        this.order = order;
        this.isChecked = false;
    }
    public String getId() {
        return this.id;
    }
    public String getName() {
        return this.name;
    }
    public String getCatogory() {
        return this.catogory;
    }
    public int getOrder()   {
        return this.order;
    }
    public boolean isChecked()  {
        return this.isChecked;
    }
    public void setChecked(boolean checked)    {
        this.isChecked = checked;
    }
}
