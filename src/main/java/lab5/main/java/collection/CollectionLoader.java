package lab5.main.java.collection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import lab5.main.java.data.LabWork;
import lab5.main.java.exception.FileLoadingException;
import lab5.main.java.util.ZonedDateTimeAdapter;

import java.io.BufferedReader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.ArrayList;

public class CollectionLoader {
    private final String filePath;
    private final Gson gson;

    public CollectionLoader(String filePath) {
        this.filePath = filePath;

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(java.time.ZonedDateTime.class, new ZonedDateTimeAdapter());
        gsonBuilder.setPrettyPrinting();
        this.gson = gsonBuilder.create();
    }

    public List<LabWork> loadCollection() throws FileLoadingException {
        System.out.println("Loading collection from file: " + filePath);

        try {
            if (filePath == null || filePath.isEmpty()) {
                throw new FileLoadingException("File path is not set. Please set the environment variable.");
            }

            if (!Files.exists(Paths.get(filePath))) {
                System.out.println("File not found: " + filePath + ". Starting with an empty collection.");
                return new ArrayList<>();
            }

            if (!Files.isReadable(Paths.get(filePath))) {
                throw new FileLoadingException("Cannot read file: " + filePath + ". Check file permissions.");
            }


            StringBuilder jsonContent = new StringBuilder();
            try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonContent.append(line);
                }
            }

            if (jsonContent.isEmpty() || jsonContent.toString().trim().isEmpty()) {
                System.out.println("File is empty or contains only whitespace. Starting with an empty collection.");
                return new ArrayList<>();
            }

            Type collectionType = new TypeToken<List<LabWork>>(){}.getType();
            System.out.println("JSON content (partial or full for debug): " + jsonContent.substring(0, Math.min(jsonContent.length(), 500)) + "..."); // Ограничиваем вывод для больших файлов
            List<LabWork> labWorks = gson.fromJson(jsonContent.toString(), collectionType);

            if (labWorks == null) {
                System.out.println("JSON content was 'null'. Starting with an empty collection.");
                return new ArrayList<>();
            }

            return labWorks;


        } catch (JsonSyntaxException e) {
            throw new FileLoadingException("Error parsing JSON syntax in file " + filePath + ": " + e.getMessage());
        } catch (java.io.IOException e) {
            throw new FileLoadingException("Error reading or accessing file " + filePath + ": " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An unexpected error occurred during file loading: " + e.getMessage());
            throw new FileLoadingException("An unexpected error occurred during file loading: " + e.getMessage());
        }
    }
}