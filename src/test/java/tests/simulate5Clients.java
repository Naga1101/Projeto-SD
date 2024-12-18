package tests;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import client.Client;

public class simulate5Clients {
    public static void main(String[] args) {
        startServer();

        int numberOfClients = 5;

        List<String> simulationResults = new ArrayList<>();

        Thread[] clientThreads = new Thread[numberOfClients];
        for (int i = 0; i < numberOfClients; i++) {
            final int clientId = i + 1;

            clientThreads[i] = new Thread(() -> {
                System.out.println("Simulating client " + clientId + "...");
                try (Client client = new Client()) {
                    client.startClient();

                    synchronized (simulationResults) {
                        simulationResults.add("Client " + clientId + " completed tasks successfully.");
                    }
                } catch (Exception e) {
                    System.err.println("Error with client " + clientId + ": " + e.getMessage());
                }
            });

            clientThreads[i].start();
        }

        for (Thread thread : clientThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("All clients have finished.");
        
        System.out.println("Simulation Results:");
        for (String result : simulationResults) {
            System.out.println(result);
        }
    }

    private static void startServer() {
        System.out.println("Starting the server...");

        String javaCommand = "java"; 
        String serverClass = "Server"; 
        String maxClients = "5";
        String numWorkers = "4";
        String numSchedulers = "2";
        String numDispatchers = "3";
        String typeDB = "0";

        ProcessBuilder processBuilder = new ProcessBuilder(
                javaCommand,
                serverClass,
                maxClients,
                numWorkers,
                numSchedulers,
                numDispatchers,
                typeDB
        );

        processBuilder.directory(new java.io.File("src/main/java/server"));

        try {
            Process process = processBuilder.start();

            new Thread(() -> logProcessOutput(process.getInputStream(), "SERVER OUTPUT")).start();
            new Thread(() -> logProcessOutput(process.getErrorStream(), "SERVER ERROR")).start();

            Thread.sleep(5000);

            System.out.println("Server started successfully!");

        } catch (Exception e) {
            System.err.println("Error while starting the server:");
            e.printStackTrace();
        }
    }

    private static void logProcessOutput(InputStream inputStream, String label) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(label + ": " + line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
