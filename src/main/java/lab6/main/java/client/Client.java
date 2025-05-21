package lab6.main.java.client;

import lab6.main.java.command.ExecuteScriptCommand;
import lab6.main.java.data.*;
import lab6.main.java.network.CommandRequest;
import lab6.main.java.network.CommandResponse;
import lab6.main.java.util.InputManager;
import lab6.main.java.util.OutputManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Client {
    private final String host;
    private final int port;
    private final InputManager inputManager;
    private final OutputManager outputManager;
    private final int timeout = 5000;
    private final int maxRetries = 3;
    private final Set<String> validCommands = new HashSet<>(Arrays.asList(
            "help", "info", "show", "add", "update", "remove_by_id", "clear",
            "shuffle", "reorder", "sort", "remove_all_by_difficulty",
            "remove_any_by_difficulty", "filter_greater_than_minimal_point",
            "execute_script", "exit"
    ));

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
        this.outputManager = new OutputManager(System.out);
        this.inputManager = new InputManager(outputManager);
    }

    public void start() {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(timeout);
            outputManager.println("Client started. Enter commands (type 'help' for available commands):");

            ExecuteScriptCommand executeScriptCommand = new ExecuteScriptCommand(inputManager, outputManager, host, port, timeout, maxRetries);

            while (true) {
                String line = inputManager.readLine("> ");
                if (line == null || line.trim().isEmpty()) continue;
                if (line.equals("exit")) {
                    outputManager.println("Client shutting down...");
                    break;
                }

                if (!processCommand(line, socket, executeScriptCommand)) {
                    if (line.startsWith("exit")) {
                        break; // Завершаем работу, если exit отправлен на сервер и обработан
                    }
                    continue;
                }
            }
        } catch (Exception e) {
            outputManager.println("Client error: " + e.getMessage());
        }
    }

    private boolean processCommand(String line, DatagramSocket socket, ExecuteScriptCommand executeScriptCommand) {
        String[] parts = line.trim().split("\\s+", 2);
        String commandName = parts[0].toLowerCase();
        String argument = parts.length > 1 ? parts[1] : "";

        if (!validCommands.contains(commandName)) {
            outputManager.println("Unknown command: " + commandName + ". Type 'help' for available commands.");
            return false;
        }

        if (commandName.equals("help")) {
            outputManager.println("Available commands:\n" +
                    "help : display available commands\n" +
                    "info : display collection information\n" +
                    "show : display all elements in the collection\n" +
                    "add : add a new LabWork\n" +
                    "update id : update LabWork with specified id\n" +
                    "remove_by_id id : remove LabWork by id\n" +
                    "clear : clear the collection\n" +
                    "shuffle : shuffle the collection\n" +
                    "reorder : reverse the collection order\n" +
                    "sort : sort the collection by id\n" +
                    "remove_all_by_difficulty difficulty : remove all LabWorks with specified difficulty\n" +
                    "remove_any_by_difficulty difficulty : remove one LabWork with specified difficulty\n" +
                    "filter_greater_than_minimal_point minimalPoint : display LabWorks with minimalPoint greater than specified\n" +
                    "execute_script file_name : execute script from file\n" +
                    "exit : exit the client");
            return false;
        }

        if (commandName.equals("save")) {
            outputManager.println("Command 'save' is not available on the client.");
            return false;
        }

        if (commandName.equals("execute_script")) {
            return executeScriptCommand.execute(argument, socket);
        }

        CommandRequest request = createCommandRequest(commandName, argument);
        if (request == null) {
            return false;
        }

        return sendRequest(socket, request);
    }

    private CommandRequest createCommandRequest(String commandName, String argument) {
        if (commandName.equals("add") || commandName.equals("update")) {
            try {
                LabWork labWork = inputManager.getLabWorkFromInput();
                if (labWork == null) {
                    outputManager.println("Failed to create LabWork.");
                    return null;
                }
                return new CommandRequest(commandName, commandName.equals("update") ? argument : "", labWork);
            } catch (Exception e) {
                outputManager.println("Error creating LabWork: " + e.getMessage());
                return null;
            }
        }

        return new CommandRequest(commandName, argument, null);
    }

    private boolean sendRequest(DatagramSocket socket, CommandRequest request) {
        int retries = 0;
        while (retries < maxRetries) {
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(request);
                oos.flush();
                byte[] data = baos.toByteArray();

                DatagramPacket packet = new DatagramPacket(data, data.length, InetAddress.getByName(host), port);
                socket.send(packet);

                byte[] buffer = new byte[65536];
                DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length);
                socket.receive(responsePacket);

                ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(buffer, 0, responsePacket.getLength()));
                CommandResponse response = (CommandResponse) ois.readObject();

                outputManager.println(response.getMessage());
                if (response.getCollection() != null) {
                    response.getCollection().forEach(labWork -> outputManager.println(labWork.toString()));
                }
                return response.isSuccess() || request.getCommandName().equals("exit");

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