package client;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class FileParser {
    public static byte[] stringToByte(String str) {
        return str.getBytes(StandardCharsets.UTF_8);
    }


    public static Map<String, byte[]> parseFileToMap(String filePath) throws IOException {
        Map<String, byte[]> map = new HashMap<>();

        List<String> lines = Files.readAllLines(Paths.get(filePath));

        for (String line : lines) {
            String[] parts = line.split("=", 2);
            if (parts.length == 2) {
                String key = parts[0].trim();
                String hexData = parts[1].trim();

                map.put(key, stringToByte(hexData));
            }
        }
        return map;
    }

    public static Set<String> parseFileToSet(String filePath) throws IOException {
        Set<String> set = new HashSet<>();

        List<String> lines = Files.readAllLines(Paths.get(filePath));

        for (String line : lines) {
            set.add(line.trim());
        }

        return set;
    }
}