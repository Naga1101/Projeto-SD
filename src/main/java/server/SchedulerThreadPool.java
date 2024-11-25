package server;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static server.Server.unscheduledHighPriority;
import static server.Server.unscheduledMediumPriority;
import static server.Server.unscheduledLowPriority;

public class SchedulerThreadPool {
    private final Thread[] schedulers;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition allTasksFinished = lock.newCondition();

    private boolean endPool = false;
    private int activeTaskCount = 0;

    public SchedulerThreadPool(int numberOfThreads, int bufferSize) {
        schedulers = new Thread[numberOfThreads];
        for (int i = 0; i < numberOfThreads; i++) {
            schedulers[i] = new Worker();
            schedulers[i].start();
        }
    }

    public void closePool() {
        lock.lock();
        try {
            endPool = true;
            for (Thread worker : schedulers) {
                worker.interrupt();
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

    private class Worker extends Thread {
        @Override
        public void run() {
            System.out.println("Scheduler with name: " + Thread.currentThread().getName());
            while (true) {
                EncapsulatedMsg task = null;
                try {
                    task = unscheduledHighPriority.pop();
                } catch (InterruptedException e) {
                    if (endPool) {
                        break;
                    }
                    Thread.currentThread().interrupt();
                }

                if (task != null) {
                    try {
                        lock.lock();
                        try {
                            System.out.println("Scheduling task: " + task + " on " + Thread.currentThread().getName());
                            // assign priority level to the task
                            activeTaskCount++;
                        } finally {
                            lock.unlock();
                        }
                    } finally {
                        lock.lock();
                        try {
                            System.out.println("Task: "  + task + " scheduled on " + Thread.currentThread().getName());
                            activeTaskCount--;
                            System.out.println("Nº of active tasks: " + activeTaskCount);
                        } finally {
                            lock.unlock();
                        }
                    }
                }
            }
        }
    }
}
