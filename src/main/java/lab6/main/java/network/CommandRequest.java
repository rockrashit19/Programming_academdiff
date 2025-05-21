package lab6.main.java.network;

import lab6.main.java.data.LabWork;

import java.io.Serializable;

public class CommandRequest implements Serializable {
    private final String commandName;
    private final String argument;
    private final LabWork labWork;

    public CommandRequest(String commandName, String argument, LabWork labWork) {
        this.commandName = commandName;
        this.argument = argument;
        this.labWork = labWork;
    }

    public String getCommandName() {
        return commandName;
    }

    public String getArgument() {
        return argument;
    }

    public LabWork getLabWork() {
        return labWork;
    }
}