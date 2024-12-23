package tests;

import client.SimplifiedClient;

public class testClient implements Runnable {
    private final String commandPath;
    private long duration;

    public testClient(String commandPath) {
        this.commandPath = commandPath;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        try (SimplifiedClient client = new SimplifiedClient()) {
            client.startClient(commandPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        duration = endTime - startTime;
        System.out.println("Client with command file " + commandPath + " completed in " + duration + " ms.");
    }

    public long getDuration() {
        return duration;
    }

    public String getPath(){
        return commandPath;
    }
}