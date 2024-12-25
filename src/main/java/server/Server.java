package server;

import utils.BoundedBuffer;
import utils.LogCommands;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Server {
    private static final int PORT = 12345;

    // Variaveis relativas a autenticação de users

    private static int currentOnlineUsers = 0;
    private static int userIdCounter = 1;
    private static final Lock lockCounter = new ReentrantLock();
    private static UsersAuthenticator usersAuthenticator;
    private static HashMap<Integer, WaintingUsers> mapWaitingUsers = new HashMap<>();
    private static final List<Integer> arrivalOrder = new LinkedList<>();
    private static final Lock waitingUsersLock = new ReentrantLock();
    private static Condition waitingQueueCondition = waitingUsersLock.newCondition();
    private static Lock usersBufferMapLock = new ReentrantLock(); 
    private static HashMap<String, BoundedBufferWithLock> usersOutputBuffer = new HashMap<>();
    private static LogCommands commandLogs = new LogCommands();

    // variaveis relativas à gestão de dados
    public static DBInterface.DB db;
    private static final int BUFFERSIZE = 25;
    public static SortedBoundedBuffer<ScheduledTask> unscheduledTaks = new SortedBoundedBuffer<>(BUFFERSIZE);
    public static BoundedBuffer<EncapsulatedMsg> finishedTasks = new BoundedBuffer<>(BUFFERSIZE);

    // Variaveis que são definidas ao ligar o server
    private static int numWorkers;
    private static int numSchedulers;
    private static int numDispatchers;
    private static int maxConcurrentUsers;
    private static int typeDB;

    // relativo aos workers
    private static int workersCapped = 0;


    public static void main(String[] args) {
        if (args.length == 0) {  // introduzir manualmente os argumentos
            Scanner scanner = new Scanner(System.in);

            System.out.println("Insira o número de clientes que podem estar conectados em simultâneo: ");
            maxConcurrentUsers = scanner.nextInt();
            System.out.println("Insira o número de workers que pretende: ");
            numWorkers = scanner.nextInt();
            System.out.println("Insira o número de schedulers que pretende: ");
            numSchedulers = scanner.nextInt();
            System.out.println("Insira o número de dispatchers que pretende: ");
            numDispatchers = scanner.nextInt();
            System.out.println("Insira o número do tipo de base de dados pretende utilizar(Single Lock - 0 | Lock per Key - 1 | DB com Batch - tamanho da batch): ");
            typeDB = scanner.nextInt();
            scanner.close();

            System.out.println("O server vai ter um total de " + numWorkers + " workers, "
                    + numSchedulers + " schedulers, um total de " + numDispatchers + " dispatchers e podem existir no máximo "
                    + maxConcurrentUsers + " clientes em simutâneo!");
        } else if (args.length == 5) {  // os argumentos já vêm ao criar o server
            try {
                maxConcurrentUsers = Integer.parseInt(args[0]);
                numWorkers = Integer.parseInt(args[1]);
                numSchedulers = Integer.parseInt(args[2]);
                numDispatchers = Integer.parseInt(args[3]);
                typeDB = Integer.parseInt(args[4]);

                System.out.println("O server vai ter um total de " + numWorkers + " workers, "
                        + numSchedulers + " schedulers, um total de " + numDispatchers + " dispatchers e podem existir no máximo "
                        + maxConcurrentUsers + " clientes em simutâneo!");
            } catch (NumberFormatException e) {
                System.err.println("Erro: Todos os argumentos devem ser números inteiros.");
                System.out.println("Uso correto: java Server maxXClients numWorkers numSchedulers numDispatchers");
            }
        } else {
            System.err.println("Erro: Número incorreto de argumentos fornecido.");
            System.out.println("Uso correto: java Server maxXClients numWorkers numSchedulers numDispatchers");
        }

        usersAuthenticator = new UsersAuthenticator();
        int reply;

        reply = usersAuthenticator.registerUser("admin1", "123");
        reply = usersAuthenticator.registerUser("admin2", "123");
        reply = usersAuthenticator.registerUser("admin3", "123");

        System.out.println(usersAuthenticator);

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
                int id = getNextId();

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
                    new Thread(new ClientHandler(nextUser.getMySocket(), usersAuthenticator, commandLogs)).start();
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
        
        switch (typeDB) {
            case 0:
                db = new DataBase(databaseLogFile);
                System.out.println("Escolheu a base de dados com apenas um lock global!");
                break;
            case 1:
                db = new DataBaseSingleKeyLocking(databaseLogFile);
                System.out.println("Escolheu a base de dados com um lock global e um lock por chave!");
                break;
            default:
                db = new DataBaseWithBatch(databaseLogFile, typeDB);
                System.out.println("Escolheu a base de dados com uma batch de tamanho " + typeDB + "!");
                break;
        }

        List<Worker> workers = new ArrayList<>(numWorkers);
        List<Thread> workerThreads = new ArrayList<>(numWorkers);
        var workersLock = new ReentrantLock();
        var freeWorkers = workersLock.newCondition();

        for (int i = 0; i < numWorkers; i++) {
            Worker worker = new Worker(10, workersLock, freeWorkers);
            workers.add(worker);

            Thread thread = new Thread(worker, "Worker-" + i);
            workerThreads.add(thread);
            thread.start();
        }

        SchedulerThreadPool schedulerPool = new SchedulerThreadPool(numSchedulers, workers, workersLock, freeWorkers);
        try {
            schedulerPool.awaitTaskPool();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        DispatcherThreadPool dispatcherPool = new DispatcherThreadPool(numDispatchers);
    }

    // Para todas as threads terem acesso aos outputbuffers dos users

    public static void addUserOutputBuffer(String name, BoundedBuffer outputBuffer){
        usersBufferMapLock.lock();
        try{
            usersOutputBuffer.put(name, new BoundedBufferWithLock(outputBuffer));
        } finally {
            usersBufferMapLock.unlock();
        }
    }

    public static void removeUserOutputBuffer(String name){
        usersBufferMapLock.lock();
        try{
            usersOutputBuffer.remove(name);
        } finally {
            usersBufferMapLock.unlock();
        }
    }

    public static BoundedBufferWithLock getUserOutputBuffer(String name){
        usersBufferMapLock.lock();
        try{
            return usersOutputBuffer.get(name);
        } finally {
            usersBufferMapLock.unlock();
        }
    }

    // incrementador decrementadores e gets

    public static int getNextId() {
        lockCounter.lock();
        try {
            return userIdCounter++;
        } finally {
            lockCounter.unlock();
        }
    }

    public static int getWorkersCapped(){
        return workersCapped;
    }

    public static void decrementCappedWorkers(){
        workersCapped--;
    }

    public static void incrementCappedWorkers(){
        workersCapped++;
    }
}