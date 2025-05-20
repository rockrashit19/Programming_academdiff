package lab5.main.java.util;

import lab5.main.java.data.*;
import lab5.main.java.exception.InvalidDataException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Objects;

public class InputManager {
    private final BufferedReader reader;
    private final OutputManager outputManager;

    public InputManager(OutputManager outputManager) {
        this.reader = new BufferedReader(new InputStreamReader(System.in));
        this.outputManager = outputManager;
    }

    public String readLine(String prompt) {
        outputManager.print(prompt);
        try {
            return reader.readLine();
        } catch (IOException e) {
            outputManager.println("Error reading input: " + e.getMessage());
            return null;
        }
    }

    public String getStringInput(String prompt, boolean nullable) throws InvalidDataException {
        String input = readLine(prompt);
        if (!nullable && (input == null || input.trim().isEmpty())) {
            throw new InvalidDataException("This field cannot be empty.");
        }
        return input;
    }

    public int getIntInput(String prompt, int min) throws InvalidDataException {
        while (true) {
            String input = readLine(prompt);
            try {
                int value = Integer.parseInt(input);
                if (value > min) {
                    return value;
                } else {
                    outputManager.println("Value must be greater than " + min);
                }
            } catch (NumberFormatException e) {
                outputManager.println("Invalid number format. Please enter an integer.");
            }
        }
    }

    public Double getDoubleInput(String prompt) throws InvalidDataException {
        while (true) {
            String input = readLine(prompt);
            try {
                return Double.parseDouble(input);
            } catch (NumberFormatException e) {
                outputManager.println("Invalid number format. Please enter a double.");
            }
        }
    }

    public Long getLongInput(String prompt, boolean nullable, long min) throws InvalidDataException {
        while (true) {
            String input = readLine(prompt);
            if (nullable && (input == null || input.trim().isEmpty()) || Objects.equals(input, "null")) {
                return null;
            }
            try {
                long value = Long.parseLong(input);
                if (value > min) {
                    return value;
                } else {
                    outputManager.println("Value must be greater than " + min);
                }
            } catch (NumberFormatException e) {
                outputManager.println("Invalid number format. Please enter a long integer.");
            }
        }
    }

    public ZonedDateTime getZonedDateTimeInput(String prompt) throws InvalidDataException {
        while (true) {
            String input = readLine(prompt);
            try {
                return ZonedDateTime.parse(input);
            } catch (DateTimeParseException e) {
                outputManager.println("Invalid date/time format. Please enter a valid ZonedDateTime (e.g., 2023-10-27T10:00:00+00:00).");
            }
        }
    }

    public <T extends Enum<T>> T getEnumInput(String prompt, Class<T> enumClass, boolean nullable) throws InvalidDataException {
        outputManager.println("Available options: " + String.join(", ", Arrays.stream(enumClass.getEnumConstants()).map(Enum::name).toArray(String[]::new)));

        while (true) {
            String input = readLine(prompt);
            if (nullable && (input == null || input.trim().isEmpty()) || Objects.equals(input, "null")) {
                return null;
            }
            try {
                return Enum.valueOf(enumClass, input.toUpperCase());
            } catch (IllegalArgumentException e) {
                outputManager.println("Invalid input. Please choose from: " + String.join(", ", Arrays.stream(enumClass.getEnumConstants()).map(Enum::name).toArray(String[]::new)));
            }
        }
    }

    public LabWork getLabWorkFromInput() {
        try {
            outputManager.println("Creating a new LabWork...");

            String name = getStringInput("Enter name: ", false);
            Coordinates coordinates = getCoordinatesFromInput();

            Long minimalPoint = getLongInput("Enter minimalPoint (can be null, > 0): ", true, 0);
            Difficulty difficulty = getEnumInput("Enter difficulty (can be null): ", Difficulty.class, false);
            Person author = getPersonFromInput();

            return new LabWork(0, name, coordinates, ZonedDateTime.now(), minimalPoint, difficulty, author);
        } catch (InvalidDataException e) {
            outputManager.println("Error creating LabWork: " + e.getMessage());
            return null;
        }
    }

    public Coordinates getCoordinatesFromInput() {
        try {
            outputManager.println("Creating Coordinates...");
            Double x = getDoubleInput("Enter x coordinate: ");
            int y = getIntInput("Enter y coordinate (must be > -563): ", -563); //Corrected value.
            return new Coordinates(x, y);
        } catch (InvalidDataException e) {
            outputManager.println("Error creating Coordinates: " + e.getMessage());
            return null;
        }
    }

    public Person getPersonFromInput() {
        try {
            outputManager.println("Creating Person...");
            String name = getStringInput("Enter person's name: ", false);
            ZonedDateTime birthday = getZonedDateTimeInput("Enter birthday (ISO 8601 format): ");
            String passportID = getStringInput("Enter passport ID: ", false);
            Color eyeColor = getEnumInput("Enter eye color: ", Color.class, false);
            Color hairColor = getEnumInput("Enter hair color (can be null): ", Color.class, true);
            return new Person(name, birthday, passportID, eyeColor, hairColor);
        } catch (InvalidDataException e) {
            outputManager.println("Error creating Person: " + e.getMessage());
            return null;
        }
    }
}
