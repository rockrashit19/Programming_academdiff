package lab6.main.java.command;

import lab6.main.java.collection.CollectionManager;
import lab6.main.java.data.LabWork;
import lab6.main.java.network.CommandResponse;

public class AddCommand extends AbstractCommand {
    private final CollectionManager collectionManager;

    public AddCommand(CollectionManager collectionManager) {
        super("add", "add a new LabWork");
        this.collectionManager = collectionManager;
    }

    @Override
    public CommandResponse execute(String argument, Object... args) {
        if (!argument.isEmpty()) {
            return new CommandResponse(false, "This command doesn't require an argument.", null);
        }
        if (args.length == 0 || !(args[0] instanceof LabWork)) {
            return new CommandResponse(false, "LabWork is required for add command.", null);
        }
        LabWork labWork = (LabWork) args[0];
        collectionManager.add(labWork);
        return new CommandResponse(true, "LabWork added successfully!", null);
    }
}