package lab6.main.java.command;

import lab6.main.java.collection.CollectionManager;
import lab6.main.java.network.CommandResponse;

public class ShowCommand extends AbstractCommand {
    private final CollectionManager collectionManager;

    public ShowCommand(CollectionManager collectionManager) {
        super("show", "display all elements in the collection");
        this.collectionManager = collectionManager;
    }

    @Override
    public CommandResponse execute(String argument, Object... args) {
        if (!argument.isEmpty()) {
            return new CommandResponse(false, "This command doesn't require an argument.", null);
        }
        try {
            var collection = collectionManager.getAll();
            String message = collection.isEmpty() ? "Collection is empty." : "Collection retrieved with " + collection.size() + " elements.";
            return new CommandResponse(true, message, collection);
        } catch (Exception e) {
            return new CommandResponse(false, "Error retrieving collection: " + e.getMessage(), null);
        }
    }
}