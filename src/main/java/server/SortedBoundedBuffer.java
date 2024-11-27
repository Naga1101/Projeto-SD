package server;

import java.time.Instant;
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
        long currentTimestamp = Instant.now().toEpochMilli();
        System.out.println("AntigaTimestamp: " + currentTimestamp + " | Currenttimesamp: " + item.getScheduledTimestamp() + " | Conta: " + (currentTimestamp - item.getScheduledTimestamp()));
        lock.lock();
        try {
            while (size >= capacity) { // full
                notFull.await();
            }

            item.setRealPriority(item.getRealPriority() + (currentTimestamp - item.getScheduledTimestamp()));

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

    public void sortBuffer(){  // dar sort a cada 10 tarefas pop
        long currentTimestamp = Instant.now().toEpochMilli();
        lock.lock();
        try{
            for (int i = 0; i < size; i++) {
                list.get(i).setRealPriority(
                        list.get(i).getRealPriority() + (currentTimestamp - list.get(i).getScheduledTimestamp())
                );
            }

            list.sort(Comparator.comparing(T::getRealPriority).reversed());
        } finally {
            lock.unlock();
        }
    }

    //TODO: fazer aqui uma verificação se já chegou ao limite da prioridade
    // se sim ou passam para a frente do buffer ou adiciono a uma lista de prioridades
}
