package utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class LogCommands {
    private static final String COMMANDS_FOLDER = new File(System.getProperty("user.dir"), "logs/clients-results").getAbsolutePath();
    private static final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH.mm.ss.SSS");

    private int sessionId;
    private File sessionFolder;

    public LogCommands() {
        this.sessionId = determineNextSessionId();
        this.sessionFolder = createSessionFolder();
        System.out.println("LogCommands initialized. Session ID: " + sessionId);
    }

    public LogCommands(boolean useMostRecentSession) {
        if (useMostRecentSession) {
            this.sessionId = determineMostRecentSessionId();
            this.sessionFolder = getSessionFolder(sessionId);
        } else {
            this.sessionId = determineNextSessionId(); // Fallback behavior
            this.sessionFolder = createSessionFolder();
        }
    }

    private int determineNextSessionId() {
        File commandsDirectory = new File(COMMANDS_FOLDER);
        if (!commandsDirectory.exists()) {
            commandsDirectory.mkdirs();
            return 1;
        }

        File[] existingSessions = commandsDirectory.listFiles((dir, name) -> name.startsWith("session-") && dir.isDirectory());
        if (existingSessions == null || existingSessions.length == 0) {
            return 1;
        }

        return Arrays.stream(existingSessions)
                .map(File::getName)
                .map(name -> name.split("-")[1]) // Extract the number part
                .mapToInt(Integer::parseInt)
                .max()
                .orElse(0) + 1;
    }

    private int determineMostRecentSessionId() {
        File commandsDirectory = new File(COMMANDS_FOLDER);
        if (!commandsDirectory.exists()) {
            throw new IllegalStateException("No sessions exist! The server must create a session first.");
        }

        File[] existingSessions = commandsDirectory.listFiles((dir, name) -> name.startsWith("session-") && dir.isDirectory());
        if (existingSessions == null || existingSessions.length == 0) {
            throw new IllegalStateException("No sessions exist! The server must create a session first.");
        }

        return Arrays.stream(existingSessions)
                .map(File::getName)
                .map(name -> name.split("-")[1]) // Extract the number part
                .mapToInt(Integer::parseInt)
                .max()
                .orElseThrow(() -> new IllegalStateException("No valid session directories found."));
    }

    private File createSessionFolder() {
        File sessionFolder = new File(COMMANDS_FOLDER, "session-" + sessionId);
        if (!sessionFolder.exists()) {
            sessionFolder.mkdirs();
        }
        return sessionFolder;
    }

    private File getSessionFolder(int sessionId) {
        File sessionFolder = new File(COMMANDS_FOLDER, "session-" + sessionId);
        if (!sessionFolder.exists()) {
            throw new IllegalStateException("Session folder does not exist: " + sessionFolder.getAbsolutePath());
        }
        return sessionFolder;
    }

    public File getClientFolder(String user) {
        File clientFolder = new File(sessionFolder, user);
        if (!clientFolder.exists()) {
            clientFolder.mkdirs();
        }
        return clientFolder;
    }

    public void logCommand(String user, String comando, long time) {
        File clientFolder = getClientFolder(user);
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
        String timestamp = dateTime.format(timeFormat);
        String commandFileName = String.format("%d-%s-%s.txt", time, timestamp, comando);
        File commandLogFile = new File(clientFolder, commandFileName);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(commandLogFile, true))) {
            writer.write("Comando: " + comando + " | " + " Hora de Pedido: " + timestamp);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Failed to write command log: " + e.getMessage());
        }
    }

    public void logReply(String user, String comando, long time, String key, String data) {
        File clientFolder = getClientFolder(user);
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault());
        String timestamp = dateTime.format(timeFormat);
        String commandFileName = String.format("%d-%s-%s.txt", time, timestamp, comando);
        File commandLogFile = new File(clientFolder, commandFileName);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(commandLogFile, true))) {
            writer.write("Hora de Pedido: " + timestamp + "| comando: " + comando + " | chave: " + key + " | data: " + data);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Failed to write reply to command log: " + e.getMessage());
        }
    }
}
