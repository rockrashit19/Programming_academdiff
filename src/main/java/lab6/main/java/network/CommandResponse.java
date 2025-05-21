package lab6.main.java.network;

import java.io.Serializable;
import java.util.List;

public class CommandResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private final boolean success;
    private final String message;
    private final List<?> collection;

    public CommandResponse(boolean success, String message, List<?> collection) {
        this.success = success;
        this.message = message;
        this.collection = collection;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public List<?> getCollection() {
        return collection;
    }
}