package server;

import utils.BoundedBuffer;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
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
    public static DataBaseWithBatch db;
    private static int numWorkers;
    private static int numSchedulers;
    private static final int BUFFERSIZE = 25;
    public static SortedBoundedBuffer<ScheduledTask> unscheduledTaks = new SortedBoundedBuffer<>(BUFFERSIZE);


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
        System.out.println("Insira o número de workers que pretende: ");
        numWorkers = scanner.nextInt();
        System.out.println("Insira o número de schedulers que pretende: ");
        numSchedulers = scanner.nextInt();
        scanner.close();

        System.out.println("O server vai ter um total de " + numWorkers + " workers, "
         + numSchedulers + " schedulers e podem existir no máximo " + maxConcurrentUsers + " clientes em simutâneo!");

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
            System.out.println("Current online users: " + currentOnlineUsers);
            System.out.println(usersAuthenticator);
        } finally {
            waitingUsersLock.unlock();
        }
    }

    // loop de gestão de dados

    public static void backgroundLoop(){
        Logs databaseLogFile = new Logs();
        db = new DataBaseWithBatch(databaseLogFile, 50);

        List<Worker> workers = new ArrayList<>(numWorkers); 
        List<Thread> workerThreads = new ArrayList<>(numWorkers);

        for (int i = 0; i < numWorkers; i++) {
            Worker worker = new Worker(5);
            workers.add(worker);

            Thread thread = new Thread(worker, "Worker-" + i);
            workerThreads.add(thread);
            thread.start();
        }

        SchedulerThreadPool schedulerPool = new SchedulerThreadPool(numSchedulers, 30, workers);
        try {
            schedulerPool.awaitTaskPool();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}