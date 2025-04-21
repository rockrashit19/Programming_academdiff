package lab5.main.java.collection;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import lab5.main.java.data.LabWork;
import lab5.main.java.exception.FileLoadingException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class CollectionLoader {
    private final String filePath;

    public CollectionLoader(String filePath) {
        this.filePath = filePath;
    }

    public List<LabWork> loadCollection() throws FileLoadingException {
        System.out.println("Loading collection from file: " + filePath);


        try {
            if (filePath == null || filePath.isEmpty()) {
                throw new FileLoadingException("File path is not set.  Please set the environment variable.");
            }

            if (!Files.exists(Paths.get(filePath))) {
                throw new FileLoadingException("File not found: " + filePath);
            }

            if (!Files.isReadable(Paths.get(filePath))) {
                throw new FileLoadingException("Cannot read file: " + filePath + ".  Check file permissions.");
            }


            StringBuilder jsonContent = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(filePath))))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonContent.append(line).append("\n");
                }
            }

            if (jsonContent.length() == 0) {
                System.out.println("File is empty.  Starting with an empty collection.");
                return List.of();
            }

            Gson gson = new Gson();
            Type collectionType = new TypeToken<List<LabWork>>(){}.getType();
            System.out.println("JSON content: " + jsonContent.toString());
            List<LabWork> labWorks = gson.fromJson(jsonContent.toString(), collectionType);
            System.out.println("Loaded labWorks: " + labWorks);

            if (labWorks == null) {
                return List.of();
            }


            return labWorks;


        } catch (JsonSyntaxException e) {
            throw new FileLoadingException("Error parsing JSON: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error loading collection: " + e.getMessage());
            throw new FileLoadingException("Error loading collection from file: " + e.getMessage());
        }
    }
}
