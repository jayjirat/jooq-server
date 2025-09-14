package jooq.server.myserver.exceptions;

public class DslMethodLoadException extends Exception {

    public DslMethodLoadException() {
        super();
    }

    public DslMethodLoadException(String message) {
        super(message);
    }

    public DslMethodLoadException(String message, Throwable cause) {
        super(message, cause);
    }

    public DslMethodLoadException(Throwable cause) {
        super(cause);
    }
}
