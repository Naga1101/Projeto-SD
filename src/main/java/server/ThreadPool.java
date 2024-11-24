package server;

import utils.BoundedBuffer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadPool {
    private final Thread[] workers;
    private final BoundedBuffer<String> taskBuffer;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition allTasksFinished = lock.newCondition();

    private boolean endPool = false;
    private int activeTaskCount = 0;

    public ThreadPool(int numberOfThreads, int bufferSize) {
        workers = new Thread[numberOfThreads];
        taskBuffer = new BoundedBuffer<>(bufferSize);
        for (int i = 0; i < numberOfThreads; i++) {
            workers[i] = new Worker();
            workers[i].start();
        }
    }

    public void submitTask(String task) throws InterruptedException {
        lock.lock();
        try {
            if (endPool) {
                throw new IllegalStateException("Thread pool is shut down");
            }
            activeTaskCount++;
            taskBuffer.push(task);
        } finally {
            lock.unlock();
        }
    }

    public void closePool() {
        lock.lock();
        try {
            endPool = true;
            for (Thread worker : workers) {
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
            while (true) {
                String task = null;
                try {
                    task = taskBuffer.pop(); 
                } catch (InterruptedException e) {
                    if (endPool) {
                        break;
                    }
                    Thread.currentThread().interrupt();
                }

                if (task != null) {
                    try {
                        System.out.println("Executing task: " + task + " on " + Thread.currentThread().getName());
                    } finally {
                        lock.lock();
                        try {
                            activeTaskCount--;
                            if (activeTaskCount == 0) {
                                allTasksFinished.signalAll();
                            }
                        } finally {
                            lock.unlock();
                        }
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        ThreadPool pool = new ThreadPool(10, 100);

        for (int i = 0; i < 50; i++) {
            String task = "Task " + i;
            try {
                pool.submitTask(task);
                //Thread.sleep(100);
            } catch (InterruptedException e) {
                System.out.println("Failed to submitTask: " + task);
                Thread.currentThread().interrupt();
            }

        }

        pool.closePool();
        try {
            pool.awaitTaskPool();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("All tasks completed. Thread pool shut down.");
    }
}
