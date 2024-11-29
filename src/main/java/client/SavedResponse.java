package client;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class SavedResponse {
    private Object command;
    private String requestedTime;
    private String arrivedTime;
    private String key;
    private byte[] data;
    private Map<String, byte[]> multigetData;

    public SavedResponse() { }

    public SavedResponse(Object command, long requestedTime, long arrivedTime) {
        this.command = command;
        this.key = "key-set";
        this.data = "data-saved".getBytes();
        this.requestedTime = formatTimestamp(requestedTime);
        this.arrivedTime = formatTimestamp(arrivedTime);
    }


    public SavedResponse(Object command, String key, long requestedTime, long arrivedTime) {
        this.command = command;
        this.key = key;
        this.data = "data-saved".getBytes();
        this.requestedTime = formatTimestamp(requestedTime);
        this.arrivedTime = formatTimestamp(arrivedTime);
    }

    public SavedResponse(Object command, String key, byte[] data, long requestedTime, long arrivedTime) {
        this.command = command;
        this.key = key;
        this.data = data;
        this.requestedTime = formatTimestamp(requestedTime);
        this.arrivedTime = formatTimestamp(arrivedTime);
    }

    public SavedResponse(Object command, Map<String, byte[]> multigetData, long requestedTime, long arrivedTime) {
        this.command = command;
        this.multigetData = multigetData;
        this.requestedTime = formatTimestamp(requestedTime);
        this.arrivedTime = formatTimestamp(arrivedTime);
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