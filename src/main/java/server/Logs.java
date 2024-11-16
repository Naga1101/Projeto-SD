package server;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logs {
    private static final String LOG_FOLDER = "../logs";
    private static final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH.mm.ss.SSS");
    
    private File sessionLogFile;
    private int sessionId;

    public Logs() {
        this.sessionId = determineNextSessionId();
        this.sessionLogFile = createLogFile(); 
        writeSessionHeader();
    }

    private int determineNextSessionId() {
        File logDirectory = new File(LOG_FOLDER);
        if (!logDirectory.exists()) {
            logDirectory.mkdirs();
            return 1;
        }
    
        File[] existingLogs = logDirectory.listFiles((dir, name) -> name.startsWith("log-") && name.endsWith(".txt"));
        if (existingLogs == null || existingLogs.length == 0) {
            return 1;
        }
    
        return Arrays.stream(existingLogs)
                .map(File::getName)
                .map(name -> name.split("-")[1]) 
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(0) + 1;
    }

    private File createLogFile() {
        String timestamp = LocalDateTime.now().format(timeFormat);
        String logFileName = String.format("log-%d-%s.txt", sessionId, timestamp);
        File sessionLogFile = new File(LOG_FOLDER, logFileName);

        try {
            if (sessionLogFile.createNewFile()) {
                System.out.println("Log file created: " + sessionLogFile.getAbsolutePath());
            }
        } catch (IOException e) {
            System.err.println("Failed to create log file: " + e.getMessage());
        }
        return sessionLogFile;
    }

    private void writeSessionHeader() {
        String timeFormatstamp = LocalDateTime.now().format(timeFormat);
        String header = String.format("Server Started at: %s%nCommands made during session:%n", timeFormatstamp);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(sessionLogFile, StandardCharsets.UTF_8, true))) { 
            writer.write(header);  // Write session header to log file
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Failed to write session header: " + e.getMessage());
        }
    }

    public void log(String message) {
        String timestamp = LocalDateTime.now().format(timeFormat);
        String logMessage = timestamp + " - " + message;
    
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(sessionLogFile, StandardCharsets.UTF_8, true))) {  
            writer.write(logMessage);  
            writer.newLine();  
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        }
    }

    // public static void main(String[] args) {
    //     Logs logger = new Logs();
    //     logger.log("teste");
    // }
}
