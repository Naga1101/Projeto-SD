package server;

public class Timer {
    private static final int TIMEOUT = 180000; // 3 minutos
    private Thread countdownThread;
    private Runnable onTimeout;

    // precisa disto para conseguir colocar o user offline se exceder o timeout
    private UsersAuthenticator usersAuthenticator;
    private String username;
    private boolean isStopped = false;

    public Timer(Runnable onTimeout, UsersAuthenticator usersAuthenticator) {
        this.onTimeout = onTimeout;
        this.usersAuthenticator = usersAuthenticator;
        this.username = null;
    }

    public void assignUsernameToTimer(String username) {
        this.username = username;
    }

    public void startCountdown() {
        if (countdownThread != null && countdownThread.isAlive()) {
            countdownThread.interrupt();
        }

        isStopped = false;
        countdownThread = new Thread(() -> {
            try {
                Thread.sleep(TIMEOUT);
                if(!isStopped && usersAuthenticator != null) {
                    int reply = usersAuthenticator.logUserOut(username);
                }

                if (!isStopped) {
                    Server.clientDisconnected();
                    onTimeout.run();
                }

            } catch (InterruptedException e) {
                if (!isStopped) {
                    System.out.println("User is active!");
                }
            }
        });
        countdownThread.start();
    }

    public void resetCountdown() {
        //System.out.println("Vou recomeçar o timer");
        startCountdown(); 
    }

    public void stopCountdown() {
        isStopped = true;
        if (countdownThread != null && countdownThread.isAlive()) {
            countdownThread.interrupt();
        }
    }
}
