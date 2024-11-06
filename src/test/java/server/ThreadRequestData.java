package server;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadRequestData {
    private final int threadId;
    private final List<Integer> requestedKeyCounts = new ArrayList<>();
    private final List<Set<String>> requestedKeySets = new ArrayList<>();

    public ThreadRequestData(int threadId) {
        this.threadId = threadId;
    }

    public List<Integer> getRequestedKeyCounts() {
        return requestedKeyCounts;
    }

    public List<Set<String>> getRequestedKeySets() {
        return requestedKeySets;
    }

    public void addRequest(int numKeys, Set<String> keys) {
        requestedKeyCounts.add(numKeys);
        requestedKeySets.add(new HashSet<>(keys));  
    }
}