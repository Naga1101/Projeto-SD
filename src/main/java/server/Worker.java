package server;

import enums.Enums.WorkerStatus;
import utils.BoundedBuffer;

public class Worker {
    private final int cap;
    private WorkerStatus status;
    private BoundedBuffer<ScheduledTask> tasks;

    public Worker(int bufferSize) {
        this.status = WorkerStatus.FREE;
        this.cap = bufferSize;
        this.tasks = new BoundedBuffer<>(bufferSize);
    }

    public WorkerStatus getStatus() {
        return this.status;
    }

    public BoundedBuffer<ScheduledTask> getTasks() {
        return this.tasks;
    }

    // O boundedbuffer já tem um lock por isso pode fazer o .size sem dar lock aqui
    public int getTaskCount() {
        return this.tasks.size();
    }

    private void setStatus() {
        int currentAmount = getTaskCount();
        if (currentAmount == 0) {
            this.status = WorkerStatus.FREE;
        } else if (currentAmount == cap) {
            this.status = WorkerStatus.MAXCAPPED;
        } else {
            this.status = WorkerStatus.WORKING;
        }
    }
}
