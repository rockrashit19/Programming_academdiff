package lab6.main.java.command;

import lab6.main.java.collection.CollectionManager;
import lab6.main.java.data.LabWork;
import lab6.main.java.network.CommandResponse;

import java.util.List;

public class FilterGreaterThanMinimalPointCommand extends AbstractCommand {
    private final CollectionManager collectionManager;

    public FilterGreaterThanMinimalPointCommand(CollectionManager collectionManager) {
        super("filter_greater_than_minimal_point", "filter_greater_than_minimal_point minimalPoint : display LabWorks with minimalPoint greater than specified");
        this.collectionManager = collectionManager;
    }

    @Override
    public CommandResponse execute(String argument, Object... args) {
        if (argument.isEmpty()) {
            return new CommandResponse(false, "Usage: filter_greater_than_minimal_point <minimalPoint>", null);
        }
        try {
            long minimalPoint = Long.parseLong(argument);
            List<LabWork> filteredLabWorks = collectionManager.filterGreaterThanMinimalPoint(minimalPoint);
            return new CommandResponse(true, filteredLabWorks.isEmpty() ? "No LabWorks found with minimalPoint > " + minimalPoint : "Filtered LabWorks retrieved.", filteredLabWorks);
        } catch (NumberFormatException e) {
            return new CommandResponse(false, "Invalid minimalPoint format: " + argument, null);
        }
    }
}