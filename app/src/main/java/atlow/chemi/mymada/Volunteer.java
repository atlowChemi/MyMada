package atlow.chemi.mymada;

public class Volunteer {
    private String name;
    private int job;
    private int thumbnail;

    public Volunteer() {
    }

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

    public int getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(int thumbnail) {
        this.thumbnail = thumbnail;
    }
}
