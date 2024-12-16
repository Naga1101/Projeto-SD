package server;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CommandMapper {
    private static final Map<CommandKey, String> commandMap = new HashMap<>();

    static {
        commandMap.put(new CommandKey((byte) 0, (byte) 1), "GET");
        commandMap.put(new CommandKey((byte) 0, (byte) 2), "MULTIGET");
        commandMap.put(new CommandKey((byte) 0, (byte) 3), "GETWHEN");
        commandMap.put(new CommandKey((byte) 1, (byte) 1), "PUT");
        commandMap.put(new CommandKey((byte) 1, (byte) 2), "MULTIPUT");
    }

    public static String getCommand(byte opcode, byte subcode) {
        return commandMap.getOrDefault(new CommandKey(opcode, subcode), "UNKNOWN_COMMAND");
    }

    private static class CommandKey {
        private final byte opcode;
        private final byte subcode;

        public CommandKey(byte opcode, byte subcode) {
            this.opcode = opcode;
            this.subcode = subcode;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            CommandKey that = (CommandKey) obj;
            return opcode == that.opcode && subcode == that.subcode;
        }

        @Override
        public int hashCode() {
            return Objects.hash(opcode, subcode);
        }
    }
}
