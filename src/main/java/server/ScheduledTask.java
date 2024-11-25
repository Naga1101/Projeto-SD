package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScheduledTask<T>{
    private T task;
    private int basePriority;
    private int realPriority;
    private long scheduledTimestamp;

    //to be filled in deserialize
    ScheduledTask () {
    }

    ScheduledTask(T task) {
        this.task = task;
        this.scheduledTimestamp = Instant.now().toEpochMilli();
    }

    public T getMessage() {
        return this.task;
    }

    public int getBasePriority(){
        return this.basePriority;
    }

    public int getRealPriority(){
        return this.realPriority;
    }

    public long getScheduledTimestamp(){
        return this.scheduledTimestamp;
    }
    
    public void setMessage(T task) {
        this.task = task;
    }
    
    public void setBasePriority(int basePriority){
        this.basePriority = basePriority;
    }

    public void setRealPriority(int realPriority){
        this.realPriority = realPriority;
    }

    public void setScheduledTimestamp(){}

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Task: ").append(task != null ? task.toString() : "null").append("\n");
        sb.append(" | Base Priority: ").append(basePriority).append("\n");
        sb.append(" | Real Priority: ").append(realPriority).append("\n");
        sb.append(" | Scheduled Timestamp: ").append(scheduledTimestamp).append("\n");
        return sb.toString();
    }

    @Override
    public ScheduledTask<T> clone() {
        ScheduledTask<T> clonedTask = new ScheduledTask<>();
        clonedTask.setMessage(this.task); // Note: Ensure T is immutable or handles cloning itself
        clonedTask.setBasePriority(this.basePriority);
        clonedTask.setRealPriority(this.realPriority);
        clonedTask.setScheduledTimestamp(); // Assign a new timestamp
        return clonedTask;
    }
}
