package server;

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class SortedBoundedBuffer<T extends ScheduledTask> {
    private List<T> list;
    private int size;
    private final int capacity;
    
    private final ReentrantLock lock;
    private final Condition notEmpty;
    private final Condition notFull;

    public SortedBoundedBuffer(int capacity) {
        this.capacity = capacity;
        this.size = 0;
        this.list = new ArrayList<>(capacity);
        this.lock = new ReentrantLock();
        this.notEmpty = lock.newCondition();
        this.notFull = lock.newCondition();
    }

    public void push(T item) throws InterruptedException {
        lock.lock();
        try {
            while (size >= capacity) { // full
                notFull.await();
            }

            int index = 0;
            while (index < size && item.getRealPriority() <= list.get(index).getRealPriority()) {
                index++;
            }

            list.add(index, item);  
            size++;

            notEmpty.signal(); 
        } finally {
            lock.unlock();
        }
    }

    public T pop() throws InterruptedException {
        lock.lock();
        try {
            while (size < 1) { 
                notEmpty.await();
            }

            T item = list.remove(0);
            size--;

            notFull.signal(); 
            return item;
        } finally {
            lock.unlock();
        }
    }
}
