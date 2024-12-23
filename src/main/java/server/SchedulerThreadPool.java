package server;

import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import enums.Enums.WorkerStatus;
import static server.Server.unscheduledTaks;

public class SchedulerThreadPool {
    private final Thread[] schedulers;
    private final List<Worker> workers;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition allTasksFinished = lock.newCondition();

    private boolean endPool = false;
    private int activeTaskCount = 0;
    private int taskCount = 0;

    public SchedulerThreadPool(int numberOfThreads, List<Worker> workers) {
        this.workers = workers;
        schedulers = new Thread[numberOfThreads];
        for (int i = 0; i < numberOfThreads; i++) {
            Scheduler scheduler = new Scheduler();
            schedulers[i] = new Thread(scheduler, "Scheduler-" + i);
            schedulers[i].start();
        }
    }

    public void closePool() {
        lock.lock();
        try {
            endPool = true;
            for (Thread Scheduler : schedulers) {
                Scheduler.interrupt();
            }
        } finally {
            lock.unlock();
        }
    }

    public void awaitTaskPool() throws InterruptedException {
        lock.lock();
        try {
            while (activeTaskCount > 0) {
                allTasksFinished.await();
            }
        } finally {
            lock.unlock();
        }
    }

    private boolean verifyIfSort(){
        return taskCount % 10 == 0;
    }

    private class Scheduler extends Thread {
        @Override
        public void run() {
            System.out.println("Scheduler with name: " + Thread.currentThread().getName());
            while (true) {
                ScheduledTask task = new ScheduledTask<EncapsulatedMsg>();
                try {
                    taskCount++;
                    if(verifyIfSort())  unscheduledTaks.sortBuffer();
                    task = unscheduledTaks.pop();
                } catch (InterruptedException e) {
                    if (endPool) {
                        break;
                    }
                    Thread.currentThread().interrupt();
                }

                if (task != null) {
                    Worker bestWorker = null;

                    for(Worker worker : workers){
                        if(worker.getStatus() == WorkerStatus.FREE){
                            bestWorker = worker;
                            break;
                        } else if(worker.getStatus() == WorkerStatus.WORKING){
                            if(bestWorker == null || bestWorker.getTaskCount() > worker.getTaskCount()){
                                bestWorker = worker;
                            }
                        }
                    }

                    if(bestWorker != null){
                        try{
                            System.out.println("O worker é " + bestWorker.getNome() + " com status " + bestWorker.getStatus());
                            bestWorker.setStatus(1);
                            bestWorker.getTasks().push(task);
                        } catch(Exception e){
                            System.out.println(getName() + e);
                        }
                    }
                    else {
                        System.out.println("não existem workers disponiveis");
                    }
                }
            }
        }
    }
}
