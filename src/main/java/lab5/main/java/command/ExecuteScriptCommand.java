package lab5.main.java.command;

import lab5.main.java.util.*;
import lab5.main.java.exception.ScriptRecursionException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class ExecuteScriptCommand extends AbstractCommand{
    private final CommandManager commandManager;
    private final OutputManager outputManager;
    private final Set<String> scriptStack = new HashSet<>();

    public ExecuteScriptCommand(CommandManager commandManager, OutputManager outputManager) {
        super("execute_script", "execute_script file_name : считать и исполнить скрипт из указанного файла");
        this.commandManager = commandManager;
        this.outputManager = outputManager;
    }

    @Override
    public boolean execute(String argument) {
        if (argument == null || argument.isEmpty()) {
            outputManager.println("Usage: execute_script <file_name>");
            return false;
        }

        File scriptFile = new File(argument);

        if (!scriptFile.exists() || !scriptFile.isFile()) {
            outputManager.println("File not found or is not a file: " + argument);
            return false;
        }

        if (!scriptFile.canRead()) {
            outputManager.println("Cannot read file: " + argument);
            return false;
        }

        String canonicalPath;
        try {
            canonicalPath = scriptFile.getCanonicalPath();
        } catch (IOException e) {
            outputManager.println("Error getting canonical path: " + e.getMessage());
            return false;
        }

        if (scriptStack.contains(canonicalPath)) {
            outputManager.println("Script recursion detected: " + argument);
            return false;
        }

        scriptStack.add(canonicalPath);

        try (BufferedReader reader = new BufferedReader(new FileReader(scriptFile))) {
            String line;
            while ((line = reader.readLine()) != null) {

                if (!line.trim().isEmpty()) {
                    outputManager.println("Executing script command: " + line);
                    if (!commandManager.executeCommand(line)) {
                        outputManager.println("Error executing command from script: " + line);
                    }
                }
            }
            scriptStack.remove(canonicalPath);
            return true;

        } catch (IOException e) {
            outputManager.println("Error reading script file: " + e.getMessage());
            scriptStack.remove(canonicalPath);
            return false;
        }
    }

}
