package atlow.chemi.mymada.classes;

public class Volunteer {
    private String name;
    private int job;

    public Volunteer(String name, int job) {
        this.name = name;
        this.job = job;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getJob() {
        return job;
    }

    public void setJob(int job) {
        this.job = job;
    }
}
