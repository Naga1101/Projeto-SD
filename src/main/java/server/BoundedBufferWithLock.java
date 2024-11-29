package server;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import utils.BoundedBuffer;

public class BoundedBufferWithLock {
    private BoundedBuffer outputBuffer;
    private Lock clientBufferLock = new ReentrantLock();

    public BoundedBufferWithLock(BoundedBuffer outputBuffer){
        this.outputBuffer = outputBuffer;
    }

    public BoundedBuffer getOutputBuffer(){
        clientBufferLock.lock();
        try{
            return this.outputBuffer;
        } finally {
            clientBufferLock.unlock();
        }
    }

    public Lock getLock(){
        return this.clientBufferLock;
    }
}
