package lab6.main.java.command;

import lab6.main.java.collection.CollectionManager;
import lab6.main.java.data.Difficulty;
import lab6.main.java.network.CommandResponse;

public class RemoveAnyByDifficultyCommand extends AbstractCommand {
    private final CollectionManager collectionManager;

    public RemoveAnyByDifficultyCommand(CollectionManager collectionManager) {
        super("remove_any_by_difficulty", "remove_any_by_difficulty difficulty : remove one LabWork with specified difficulty");
        this.collectionManager = collectionManager;
    }

    @Override
    public CommandResponse execute(String argument, Object... args) {
        if (argument.isEmpty()) {
            return new CommandResponse(false, "Usage: remove_any_by_difficulty <difficulty>\nAvailable difficulties: " + String.join(", ", Difficulty.names()), null);
        }
        try {
            Difficulty difficulty = Difficulty.valueOf(argument.toUpperCase());
            boolean success = collectionManager.removeAnyByDifficulty(difficulty);
            return new CommandResponse(success, success ? "One LabWork with difficulty " + difficulty + " removed." : "No LabWork found with difficulty " + difficulty, null);
        } catch (IllegalArgumentException e) {
            return new CommandResponse(false, "Invalid difficulty. Available difficulties: " + String.join(", ", Difficulty.names()), null);
        }
    }
}