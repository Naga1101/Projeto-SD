package client;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import utils.LogCommands;

public class SavedResponse {
    private LogCommands logCommands = new LogCommands(true);
    private Object command;
    private String requestedTime;
    private String arrivedTime;
    private String key;
    private byte[] data;
    private Map<String, byte[]> multigetData;

    public SavedResponse() { }

    public SavedResponse(Object command, long requestedTime, long arrivedTime, String username) {
        this.command = command;
        this.key = "key-set";
        this.data = "data-saved".getBytes();
        this.requestedTime = formatTimestamp(requestedTime);
        this.arrivedTime = formatTimestamp(arrivedTime);
        logCommands.logReply(username, command.toString(), requestedTime, key, "data-saved");
    }


    public SavedResponse(Object command, String key, long requestedTime, long arrivedTime, String username) {
        this.command = command;
        this.key = key;
        this.data = "data-saved".getBytes();
        this.requestedTime = formatTimestamp(requestedTime);
        this.arrivedTime = formatTimestamp(arrivedTime);
        logCommands.logReply(username, command.toString(), requestedTime, key, "data-saved");
    }

    public SavedResponse(Object command, String key, byte[] data, long requestedTime, long arrivedTime, String username) {
        this.command = command;
        this.key = key;
        this.data = (data != null) ? data : "null".getBytes();
        this.requestedTime = formatTimestamp(requestedTime);
        this.arrivedTime = formatTimestamp(arrivedTime);
        logCommands.logReply(username, command.toString(), requestedTime, key, new String(this.data));
    }
    

    public SavedResponse(Object command, Map<String, byte[]> multigetData, long requestedTime, long arrivedTime, String username) {
        this.command = command;
        this.multigetData = new HashMap<>();
        for (Map.Entry<String, byte[]> entry : multigetData.entrySet()) {
            byte[] value = entry.getValue();
            this.multigetData.put(entry.getKey(), (value != null) ? value : "null".getBytes());
        }
        this.requestedTime = formatTimestamp(requestedTime);
        this.arrivedTime = formatTimestamp(arrivedTime);
        for (Map.Entry<String, byte[]> entry : this.multigetData.entrySet()) {
            logCommands.logReply(username, command.toString(), requestedTime, entry.getKey(), new String(entry.getValue()));
        }
    }


    private String formatTimestamp(long timestamp) {
        LocalDateTime dateTime = Instant.ofEpochMilli(timestamp)
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDateTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return dateTime.format(formatter);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append(command).append(" | ")
              .append(requestedTime).append(" | ")
              .append(arrivedTime);

        if (key != null && data != null) {
            result.append(" | ").append(key).append(" | ")
                  .append(new String(data)); // Convert byte[] data to String
        }

        if (key != null && data == null) {
            result.append(" | ").append(key).append(" | ")
                  .append("null"); // Convert byte[] data to String
        }

        if (multigetData != null && !multigetData.isEmpty()) {
            for (Map.Entry<String, byte[]> entry : multigetData.entrySet()) {
                result.append("\n").append(command).append(" | ")
                      .append(requestedTime).append(" | ")
                      .append(arrivedTime).append(" | ")
                      .append(entry.getKey()).append(" | ")
                      .append(new String(entry.getValue())); // Convert byte[] to String
            }
        }

        return result.toString();
    }

    public Object getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getRequestedTime() {
        return requestedTime;
    }

    public void setRequestedTime(String requestedTime) {
        this.requestedTime = requestedTime;
    }

    public String getArrivedTime() {
        return arrivedTime;
    }

    public void setArrivedTime(String arrivedTime) {
        this.arrivedTime = arrivedTime;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public Map<String, byte[]> getMultigetData() {
        return multigetData;
    }

    public void setMultigetData(Map<String, byte[]> multigetData) {
        this.multigetData = multigetData;
    }
}