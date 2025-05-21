package lab6.main.java.command;

import lab6.main.java.collection.CollectionManager;
import lab6.main.java.data.LabWork;
import lab6.main.java.network.CommandResponse;

public class UpdateIdCommand extends AbstractCommand {
    private final CollectionManager collectionManager;

    public UpdateIdCommand(CollectionManager collectionManager) {
        super("update", "update id : update LabWork with specified id");
        this.collectionManager = collectionManager;
    }

    @Override
    public CommandResponse execute(String argument, Object... args) {
        if (argument.isEmpty()) {
            return new CommandResponse(false, "Usage: update <id>", null);
        }
        if (args.length == 0 || !(args[0] instanceof LabWork)) {
            return new CommandResponse(false, "LabWork is required for update command.", null);
        }
        try {
            long id = Long.parseLong(argument);
            LabWork labWork = (LabWork) args[0];
            if (!collectionManager.containsId(id)) {
                return new CommandResponse(false, "No LabWork found with id " + id, null);
            }
            collectionManager.update(id, labWork);
            return new CommandResponse(true, "LabWork updated successfully!", null);
        } catch (NumberFormatException e) {
            return new CommandResponse(false, "Invalid id format: " + argument, null);
        }
    }
}