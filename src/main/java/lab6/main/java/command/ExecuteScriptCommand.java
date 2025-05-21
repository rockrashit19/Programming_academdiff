package lab6.main.java.command;

import lab6.main.java.data.LabWork;
import lab6.main.java.network.CommandRequest;
import lab6.main.java.network.CommandResponse;
import lab6.main.java.util.InputManager;
import lab6.main.java.util.OutputManager;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

public class ExecuteScriptCommand {
    private final InputManager inputManager;
    private final OutputManager outputManager;
    private final String host;
    private final int port;
    private final int timeout;
    private final int maxRetries;
    private final Set<String> scriptStack = new HashSet<>();
    private static final String RELATIVE_RESOURCES_DIR = "src/main/java/lab6/resources/";

    public ExecuteScriptCommand(InputManager inputManager, OutputManager outputManager, String host, int port, int timeout, int maxRetries) {
        this.inputManager = inputManager;
        this.outputManager = outputManager;
        this.host = host;
        this.port = port;
        this.timeout = timeout;
        this.maxRetries = maxRetries;
    }

    public boolean execute(String argument, DatagramSocket socket) {
        if (argument == null || argument.isEmpty()) {
            outputManager.println("Usage: execute_script <file_name>");
            return false;
        }

        File scriptFile = new File(RELATIVE_RESOURCES_DIR + argument);
        outputManager.println("Looking for file at: " + scriptFile.getAbsolutePath());

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
            Queue<String> scriptLines = new LinkedList<>();
            String line;

            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    scriptLines.add(line);
                }
            }

            while (!scriptLines.isEmpty()) {
                String commandLine = scriptLines.poll();
                outputManager.println("Executing script command: " + commandLine);

                CommandRequest request = parseScriptCommand(commandLine, scriptLines);
                if (request == null) {
                    outputManager.println("Error parsing command from script: " + commandLine);
                    continue;
                }

                if (!sendRequest(socket, request)) {
                    outputManager.println("Error executing command from script: " + commandLine);
                    inputManager.clearScriptLines();
                    scriptStack.remove(canonicalPath);
                    return false;
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

    private CommandRequest parseScriptCommand(String commandLine, Queue<String> scriptLines) {
        String[] parts = commandLine.trim().split("\\s+", 2);
        String commandName = parts[0].toLowerCase();
        String argument = parts.length > 1 ? parts[1] : "";

        if (commandName.equals("save")) {
            outputManager.println("Command 'save' is not available on the client.");
            return null;
        }

        if (commandName.equals("execute_script")) {
            outputManager.println("Nested execute_script is not allowed in scripts.");
            return null;
        }

        if (commandName.equals("add") || commandName.equals("update")) {
            inputManager.setScriptLines(new LinkedList<>(scriptLines));
            try {
                LabWork labWork = inputManager.getLabWorkFromInput();
                if (labWork == null) {
                    outputManager.println("Failed to create LabWork from script.");
                    return null;
                }
                scriptLines.clear();
                scriptLines.addAll(inputManager.getScriptLines());
                return new CommandRequest(commandName, commandName.equals("update") ? argument : "", labWork);
            } catch (Exception e) {
                outputManager.println("Error creating LabWork from script: " + e.getMessage());
                return null;
            } finally {
                inputManager.clearScriptLines();
            }
        }

        return new CommandRequest(commandName, argument, null);
    }

    private boolean sendRequest(DatagramSocket socket, CommandRequest request) {
        int retries = 0;
        while (retries < maxRetries) {
            try {
                // Сериализация запроса
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(request);
                oos.flush();
                byte[] data = baos.toByteArray();

                // Отправка запроса
                DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(host), port);
                socket.send(packet);

                // Получение ответа
                byte[] buffer = new byte[65536];
                DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(responsePacket);

                // Десериализация ответа
                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(buffer, 0, responsePacket.getLength()));
                CommandResponse response = (CommandResponse) ois.readObject();

                // Вывод результата
                outputManager.println(response.getMessage());
                if (response.getCollection() != null) {
                    response.getCollection().forEach(labWork -> outputManager.println(labWork.toString()));
                }
                return response.isSuccess();

            } catch (SocketTimeoutException e) {
                retries++;
                outputManager.println("Server not responding, retrying (" + retries + "/" + maxRetries + ")...");
                if (retries == maxRetries) {
                    outputManager.println("Server is unavailable after " + maxRetries + " retries.");
                    return false;
                }
            } catch (Exception e) {
                outputManager.println("Error communicating with server: " + e.getMessage());
                return false;
            }
        }
        return false;
    }
}