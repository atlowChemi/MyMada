package atlow.chemi.mymada;

class mCodeKid {
    public String id;
    public String name, catogory;
    public int order;
    private boolean isChecked;
    mCodeKid(String name, String id, String catogory, int order) {
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
    boolean isChecked()  {
        return this.isChecked;
    }
    void setChecked(boolean checked)    {
        this.isChecked = checked;
    }
}
