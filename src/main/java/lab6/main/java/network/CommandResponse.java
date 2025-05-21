package lab6.main.java.network;

import lab6.main.java.data.LabWork;

import java.io.Serializable;
import java.util.List;

public class CommandResponse implements Serializable {
    private final boolean success;
    private final String message;
    private final List<LabWork> collection;

    public CommandResponse(boolean success, String message, List<LabWork> collection) {
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

    public List<LabWork> getCollection() {
        return collection;
    }
}