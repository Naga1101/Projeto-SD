package server;

import enums.Enums.WorkerStatus;
import utils.BoundedBuffer;

public class Worker implements Runnable {
    private final int cap;
    private WorkerStatus status;
    private BoundedBuffer<ScheduledTask> tasks;
    private ExecuteTask executor = new ExecuteTask();

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

    public void setStatus(int newTask) {
        int currentAmount = getTaskCount() + newTask;
        if (currentAmount == 0) {
            this.status = WorkerStatus.FREE;
        } else if (currentAmount == cap) {
            this.status = WorkerStatus.MAXCAPPED;
        } else {
            this.status = WorkerStatus.WORKING;
        }
    }

    @Override
    public void run(){
        System.out.println("Worker with name: " + Thread.currentThread().getName());
        try{
            while(true){
                ScheduledTask task = tasks.pop();

                System.out.println(Thread.currentThread().getName() + " têm estado antes do set: " + getStatus() + " está a executar a tarefa");
                if(getTaskCount() == 0) setStatus(0);
                System.out.println(Thread.currentThread().getName() + " têm estado depois do set: " + getStatus() + " está a executar a tarefa");

                executor.executeTask(task);
            }       
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
