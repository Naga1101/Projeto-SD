package server;

import enums.Enums.WorkerStatus;
import utils.BoundedBuffer;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static server.Server.decrementCappedWorkers;
import static server.Server.incrementCappedWorkers;

public class Worker implements Runnable {
    private final int cap;
    private String nome;
    private WorkerStatus status;
    private BoundedBuffer<ScheduledTask> tasks;
    private ExecuteTask executor = new ExecuteTask();
    private ReentrantLock workersLock;
    private Condition freeWorker;

    public Worker(int bufferSize, ReentrantLock workersLock, Condition freeWorker) {
        this.status = WorkerStatus.FREE;
        this.cap = bufferSize;
        this.tasks = new BoundedBuffer<>(bufferSize);
        this.workersLock = workersLock;
        this.freeWorker = freeWorker;
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

    public String getNome(){
        return nome;
    }

    public void setStatus(int newTask) {
        int currentAmount = getTaskCount() + newTask;
        if (currentAmount == 0) {
            workersLock.lock();
            try{
                decrementCappedWorkers();
                freeWorker.signalAll();
                System.out.println("estou disponivel");
            } finally {
                workersLock.unlock();
            }
            this.status = WorkerStatus.FREE;
        } else if (currentAmount == cap) {
            workersLock.lock();
            try{
                incrementCappedWorkers();
            } finally {
                workersLock.unlock();
            }
            this.status = WorkerStatus.MAXCAPPED;
        } else {
            this.status = WorkerStatus.WORKING;
        }
    }

    @Override
    public void run(){
        nome = Thread.currentThread().getName();
        System.out.println("Worker with name: " + nome);
        try{
            while(true){
                ScheduledTask task = tasks.pop();
                //Thread.sleep(100);
                System.out.println(Thread.currentThread().getName() + " têm estado" + getStatus() + " e " + getTaskCount());
                if(getTaskCount() == 0) {
                    setStatus(0);
                    System.out.println(Thread.currentThread().getName() + " definiu o estado" + getStatus());
                }

                executor.executeTask(task);
            }       
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
