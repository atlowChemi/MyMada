package atlow.chemi.mymada.classes;

public class Volunteer {
    private String name;
    private int job;
    private int thumbnail;

    public Volunteer(String name, int job, int thumbnail) {
        this.name = name;
        this.job = job;
        this.thumbnail = thumbnail;
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
