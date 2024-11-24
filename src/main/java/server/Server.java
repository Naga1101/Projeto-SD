package server;

import utils.BoundedBuffer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Server {
    private static final int PORT = 12345;

    // Variaveis relativas a autenticação de users

    private static int maxConcurrentUsers;
    private static int currentOnlineUsers = 0;
    private static AtomicInteger userIdCounter = new AtomicInteger(1);
    private static UsersAuthenticator usersAuthenticator;
    private static HashMap<Integer, WaintingUsers> mapWaitingUsers = new HashMap<>();
    private static final List<Integer> arrivalOrder = new LinkedList<>();
    private static final Lock waitingUsersLock = new ReentrantLock();
    private static Condition waitingQueueCondition = waitingUsersLock.newCondition();

    // variaveis relativas à gestão de dados
    private static DataBaseWithBatch db;
    private static final int BUFFERSIZE = 25;
    public static BoundedBuffer<EncapsulatedMsg> commandsUnschedule = new BoundedBuffer<>(BUFFERSIZE);


    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        usersAuthenticator = new UsersAuthenticator();
        int reply;

        reply = usersAuthenticator.registerUser("admin1", "123");
        reply = usersAuthenticator.registerUser("admin2", "123");
        reply = usersAuthenticator.registerUser("admin3", "123");

        System.out.println(usersAuthenticator);

        System.out.println("Insira o número de clientes que podem estar conectados em simultâneo: ");
        maxConcurrentUsers = scanner.nextInt();
        System.out.println("O número máximo de clientes é: " + maxConcurrentUsers);
        scanner.close();

        backgroundLoop();

        // começar thread para lidar com clientes à espera
        Thread waitingQueueProcessor = new Thread(() -> {
            try {
                manageWaitingQueue();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        waitingQueueProcessor.start();

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                // System.out.println("Client connected: " + clientSocket.getInetAddress());

                WaintingUsers newUser = new WaintingUsers(clientSocket);
                int id = userIdCounter.getAndIncrement();

                waitingUsersLock.lock();
                try {
                    mapWaitingUsers.put(id, newUser);
                    arrivalOrder.add(id);
                    //System.out.println("Arrival order: " + arrivalOrder);
                    //System.out.println("Waiting users: " + mapWaitingUsers);
                    // System.out.println("New user: " + newUser);
                    if(currentOnlineUsers < maxConcurrentUsers) waitingQueueCondition.signal();
                } finally {
                    waitingUsersLock.unlock();
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void manageWaitingQueue() throws InterruptedException {
        while (true) {

            waitingUsersLock.lock();
            try{
                while (currentOnlineUsers >= maxConcurrentUsers || arrivalOrder.isEmpty()) {
                    //System.out.println("Waiting for users...");
                    //System.out.println("Current users: " + currentOnlineUsers);
                    //System.out.println("Max ConcurrentUsers: " + maxConcurrentUsers);
                    //System.out.println("Arrival order: " + arrivalOrder);
                    //System.out.println("Waiting users: " + mapWaitingUsers);
                    waitingQueueCondition.await();
                }

                //System.out.println("User arrived");

                int earliestUserId = arrivalOrder.remove(0);
                WaintingUsers nextUser = mapWaitingUsers.remove(earliestUserId);

                if (nextUser != null) {
                    currentOnlineUsers++;
                    new Thread(new ClientHandler(nextUser.getMySocket(), usersAuthenticator)).start();
                    System.out.println("Current OnlineUsers: " + currentOnlineUsers);
                }

            } finally {
                waitingUsersLock.unlock();
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void clientDisconnected() {
        waitingUsersLock.lock();
        try {
            currentOnlineUsers--;
            waitingQueueCondition.signal();
            System.out.println("Client disconnected: " + currentOnlineUsers);
            System.out.println(usersAuthenticator);
        } finally {
            waitingUsersLock.unlock();
        }
    }

    // loop de gestão de dados

    public static void backgroundLoop(){
        Logs databaseLogFile = new Logs();
        db = new DataBaseWithBatch(databaseLogFile, 50);

        SchedulerThreadPool schedulerPool = new SchedulerThreadPool(10, 30);
        try {
            schedulerPool.awaitTaskPool();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}