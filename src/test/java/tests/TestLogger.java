package tests;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class TestLogger {
    private static final String LOG_FOLDER = new File(System.getProperty("user.dir"), "logs/metrics-test").getAbsolutePath();
    private TestMetrics monitor = new TestMetrics();
    private final File logFile;
    private long startTime;

    public TestLogger(int testType, long startTime) {
        this.startTime = startTime;
        File folder = new File(LOG_FOLDER);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        int logId = determineNextLogId();

        String testName = getTestName(testType);

        // Create the log file
        this.logFile = new File(folder, testName + "-" + logId + ".txt");
        try {
            if (!logFile.exists()) {
                logFile.createNewFile();
            }

            // Write the header in the log file
            writeLog("Time (ms) | CPU (%) | Mem Usage (MB)");

            // Start the timer for logging time
            this.startTime = System.currentTimeMillis();

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to create log file: " + logFile.getAbsolutePath());
        }
    }

    private int determineNextLogId() {
        File logDirectory = new File(LOG_FOLDER);
        if (!logDirectory.exists()) {
            logDirectory.mkdirs();
            return 1;
        }
    
        File[] existingLogs = logDirectory.listFiles((dir, name) -> name.endsWith(".txt"));
        if (existingLogs == null || existingLogs.length == 0) {
            return 1;
        }
    
        return Arrays.stream(existingLogs)
                .map(File::getName)
                .filter(name -> name.matches(".*-\\d+\\.txt"))
                .map(name -> name.split("-")[1].replace(".txt", "")) 
                .mapToInt(Integer::parseInt) 
                .max() 
                .orElse(0) + 1; 
    }
    
    
    public void writeLog(String message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
            writer.write(message);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // formato: Time (ms) | CPU (%) | Mem Usage (unit)
    public void logMetrics() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - startTime;

        double cpuLoad = monitor.getCpuLoad();
        long freeMemory = monitor.getFreeMemory();
        double  freeMemMB = freeMemory / (1024.0 * 1024.0);

        String logMessage = String.format("   %-6d |  %-6.2f |   %-8.2f", elapsedTime, cpuLoad, freeMemMB);

        writeLog(logMessage);
    }

    // Determine the test name based on the testType
    private String getTestName(int testType) {
        switch (testType) {
            case 1: return "Workflow1c";
            case 2: return "Workflow15cMGH";
            case 3: return "Workflow15cMPH";
            case 4: return "Workflow15cPH";
            case 5: return "Workflow25cMGH";
            case 6: return "Workflow25cMPH";
            case 7: return "Workflow25cPH";
            case 8: return "Workflow50c";
            default: return "UnknownTest";
        }
    }

    public String getLogFilePath() {
        return logFile.getAbsolutePath();
    }
}

