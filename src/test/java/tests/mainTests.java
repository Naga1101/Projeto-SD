package tests;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import client.Client;

public class mainTests {
    public static void main(String[] args) {
        int testRuns = 3; // Number of test runs
        List<String> allResults = new ArrayList<>();

        for (int i = 1; i <= testRuns; i++) {
            System.out.println("Starting test run " + i + "...");

            // Simulate 5 clients (reuse simulate5Clients logic)
            List<String> testResults = simulateTestRun();

            // Log results for this test
            synchronized (allResults) {
                allResults.add("Results for Test Run " + i + ":");
                allResults.addAll(testResults);
                allResults.add(""); // Add a blank line for separation
            }

            System.out.println("Test run " + i + " completed.");
        }

        // Save all results to a file
        saveResultsToFile(allResults, "simulation_results.txt");
        System.out.println("All test runs completed. Results saved to simulation_results.txt.");
    }

    private static List<String> simulateTestRun() {
        List<String> results = new ArrayList<>();
        int numberOfClients = 5;

        Thread[] clientThreads = new Thread[numberOfClients];
        for (int i = 0; i < numberOfClients; i++) {
            final int clientId = i + 1;

            clientThreads[i] = new Thread(() -> {
                System.out.println("Simulating client " + clientId + "...");
                try (Client client = new Client()) {
                    client.startClient();

                    // Optionally, save client results
                    synchronized (results) {
                        results.add("Client " + clientId + " completed tasks successfully.");
                    }
                } catch (Exception e) {
                    System.err.println("Error with client " + clientId + ": " + e.getMessage());
                }
            });

            clientThreads[i].start();
        }

        // Wait for all clients to finish
        for (Thread thread : clientThreads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return results;
    }

    private static void saveResultsToFile(List<String> results, String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            for (String line : results) {
                writer.write(line + System.lineSeparator());
            }
        } catch (IOException e) {
            System.err.println("Error writing results to file: " + e.getMessage());
        }
    }
}
