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

        // Método para converter um código numérico para um valor de enum
        public static autenticacao fromCode(int code) {
            for (autenticacao op : autenticacao.values()) {
                if (op.getCode() == code) {
                    return op;
                }
            }
            throw new IllegalArgumentException("Código de operação inválido: " + code);
        }
    }

    public enum getCommand {
        AUX0,
        GET,
        MULTIGET,
        GETWHEN;
    }

    public enum putCommand {
        AUX0,
        PUT,
        MULTIPUT;
    }
}