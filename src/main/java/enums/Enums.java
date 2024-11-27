package enums;

public class Enums{
    public enum autenticacao {
        AUTHREPLY(0),
        LOGIN(1),
        REGISTER(2);
        
        private final int code;
    
        autenticacao(int code) {
            this.code = code;
        }
    
        public int getCode() {
            return code;
        }
    
        public static autenticacao fromCode(int code) {
            for (autenticacao op : autenticacao.values()) {
                if (op.getCode() == code) {
                    return op;
                }
            }
            throw new IllegalArgumentException("Código de operação inválido: " + code);
        }
    }
    
    public enum optionCommand {
        AUX0(0),
        GET(1),
        PUT(2),
        EXIT(3);
    
        private final int code;
    
        optionCommand(int code) {
            this.code = code;
        }
    
        public int getCode() {
            return code;
        }
    
        public static optionCommand fromCode(int code) {
            for (optionCommand op : optionCommand.values()) {
                if (op.getCode() == code) {
                    return op;
                }
            }
            throw new IllegalArgumentException("Código de operação inválido: " + code);
        }
    }

    public enum commandType {
        GET(0),
        PUT(1),
        EXIT(2);

        private final int code;

        commandType(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static commandType fromCode(int code) {
            for (commandType op : commandType.values()) {
                if (op.getCode() == code) {
                    return op;
                }
            }
            throw new IllegalArgumentException("Código de operação inválido: " + code);
        }
    }
    
    public enum getCommand {
        AUX0(0),
        GET(1),
        MULTIGET(2),
        GETWHEN(3),
        BACK(4);
    
        private final int code;
    
        getCommand(int code) {
            this.code = code;
        }
    
        public int getCode() {
            return code;
        }
    
        public static getCommand fromCode(int code) {
            for (getCommand op : getCommand.values()) {
                if (op.getCode() == code) {
                    return op;
                }
            }
            throw new IllegalArgumentException("Código de operação inválido: " + code);
        }
    }
    
    public enum putCommand {
        AUX0(0),
        PUT(1),
        MULTIPUT(2),
        BACK(3);
    
        private final int code;
    
        putCommand(int code) {
            this.code = code;
        }
    
        public int getCode() {
            return code;
        }

        public static putCommand fromCode(int code) {
            for (putCommand op : putCommand.values()) {
                if (op.getCode() == code) {
                    return op;
                }
            }
            throw new IllegalArgumentException("Código de operação inválido: " + code);
        }
    }

    public enum TaskPriority {
        HIGH(10),    // gets e puts simples e getwhen: como são as tarefas mais rápidas então têm maior prioridade
        MEDIUM(6),  // multiget : é mais rápido que o multiput mas demora mais tempo que as tarefas simples
        LOW(2);     // multiput
        
        private final int code;
    
        TaskPriority(int code) {
            this.code = code;
        }
    
        public int getCode() {
            return code;
        }

        public static TaskPriority fromCode(int code) {
            for (TaskPriority op : TaskPriority.values()) {
                if (op.getCode() == code) {
                    return op;
                }
            }
            throw new IllegalArgumentException("Código de operação inválido: " + code);
        }
    }

    public enum WorkerStatus{
        FREE(0),
        WORKING(1),
        MAXCAPPED(2);

        private final int code;

        WorkerStatus(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static WorkerStatus fromCode(int code) {
            for (WorkerStatus op : WorkerStatus.values()) {
                if (op.getCode() == code) {
                    return op;
                }
            }
            throw new IllegalArgumentException("Código de operação inválido: " + code);
        }
    }
}