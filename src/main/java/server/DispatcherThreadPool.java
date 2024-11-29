package server;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import messagesFormat.MsgInterfaces.IMessage;
import static server.Server.finishedTasks;
import utils.BoundedBuffer;

public class DispatcherThreadPool {
    private final Thread[] dispatchers;
    private final ReentrantLock lock = new ReentrantLock();

    private boolean endPool = false;

    public DispatcherThreadPool(int numberOfThreads) {
        dispatchers = new Thread[numberOfThreads];
        for (int i = 0; i < numberOfThreads; i++) {
            Dispatcher Dispatcher = new Dispatcher();
            dispatchers[i] = new Thread(Dispatcher, "Dispatcher-" + i);
            dispatchers[i].start();
        }
    }

    public void closePool() {
        lock.lock();
        try {
            endPool = true;
            for (Thread dispatcher : dispatchers) {
                dispatcher.interrupt();
            }
        } finally {
            lock.unlock();
        }
    }

    private class Dispatcher extends Thread {
        @Override
        public void run() {
            System.out.println("Dispatcher with name: " + Thread.currentThread().getName());
            while (true) {
                try {
                    EncapsulatedMsg msg = finishedTasks.pop();

                    String user = msg.getUser();
                    BoundedBufferWithLock bufferWithLock = Server.getUserOutputBuffer(user);

                    BoundedBuffer userOutputBuffer = bufferWithLock.getOutputBuffer();
                    Lock clientBufferLock = bufferWithLock.getLock();

                    clientBufferLock.lock();
                    try{
                        IMessage reply = msg.getMessage();
                        userOutputBuffer.push(reply);
                    } finally {
                        clientBufferLock.unlock();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}