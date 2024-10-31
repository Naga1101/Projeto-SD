package server;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.concurrent.*;

public class Timer {
    private static final int TIMEOUT = 30000; // 30 seconds
    private Thread countdownThread;
    private Runnable onTimeout;

    public Timer(Runnable onTimeout) {
        this.onTimeout = onTimeout;
    }

    public void startCountdown() {
        if (countdownThread != null && countdownThread.isAlive()) {
            countdownThread.interrupt();
        }

        countdownThread = new Thread(() -> {
            try {
                Thread.sleep(TIMEOUT);
                //System.out.println("O timer chegou ao fim!");
                onTimeout.run();
            } catch (InterruptedException e) {
                //System.out.println("O timer foi interrompido vou recomeçar!");
            }
        });
        countdownThread.start();
    }

    public void resetCountdown() {
        //System.out.println("Vou recomeçar o timer");
        startCountdown(); 
    }

    public void stopCountdown() {
        if (countdownThread != null && countdownThread.isAlive()) {
            countdownThread.interrupt();
        }
    }
}
