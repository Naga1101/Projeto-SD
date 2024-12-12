package server;

import java.util.HashMap;

public class BatchFlushTimer implements Runnable {
    private final DataBaseWithBatch db;
    private final long timeThresholdMillis;
    private volatile long lastPutTimestamp; 
    private volatile boolean isRunning;

    public BatchFlushTimer(DataBaseWithBatch db, long timeThresholdSeconds) {
        this.db = db;
        this.timeThresholdMillis = timeThresholdSeconds * 1000; 
        this.lastPutTimestamp = System.currentTimeMillis();
        this.isRunning = true;
    }

    public void stopTimer() {
        isRunning = false;
    }

    public void resetTimer() {
        lastPutTimestamp = System.currentTimeMillis();
    }

    @Override
    public void run() {
        while (isRunning) {
            try {
                Thread.sleep(timeThresholdMillis);

                long currentTimestamp = System.currentTimeMillis();
                if (currentTimestamp - lastPutTimestamp >= timeThresholdMillis) {
                    // Check if batch has stale data
                    HashMap<String, byte[]> currentBatch = db.getBatch(); 
                    if (currentBatch != null && !currentBatch.isEmpty()) {
                        // System.out.println("Force flushing the batch due to inactivity.");
                        db.forceFlushBatch();
                    }

                    lastPutTimestamp = currentTimestamp;
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
