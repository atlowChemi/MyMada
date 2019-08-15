package atlow.chemi.mymada;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class Contraction {
    private Date start;
    private Date end;
    private Date oldEnd;
    private int number;
    private String previous;
    private String length;

    public Contraction(int number, String length, String previous)  {
        this.number = number;
        this.previous = previous;
        this.length = length;
    }
    public Contraction(int number, Date start, Date end, Date oldEnd)  {
        this.number = number;
        this.start = start;
        this.end = end;
        this.oldEnd = oldEnd;
    }
    public Date getStart() {
        return this.start;
    }
    void setStart(Date start) {
        this.start = start;
    }
    public Date getEnd() {
        return this.end;
    }
    void setEnd(Date end) {
        this.end = end;
    }
    public Date getOldEnd() {
        return this.oldEnd;
    }
    public void setOldEnd(Date oldEnd) {
        this.oldEnd = oldEnd;
    }
    public int getNumber() {
        return this.number;
    }
    void setNumber(int number) {
        this.number = number;
    }
    String getPrevious() {
        return this.previous;
    }
    void setPrevious(String previous) {
        this.previous = previous;
    }
    String getLength() {
        return this.length;
    }
    void setLength(String length) {
        this.length = length;
    }
}