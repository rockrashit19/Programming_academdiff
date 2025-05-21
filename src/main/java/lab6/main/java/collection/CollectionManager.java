package lab6.main.java.collection;

import lab6.main.java.data.*;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

public class CollectionManager {
    private Vector<LabWork> labWorks;
    private ZonedDateTime initializationDate;

    public CollectionManager() {
        labWorks = new Vector<>();
        initializationDate = ZonedDateTime.now();
    }

    public ZonedDateTime getInitializationDate() {
        return initializationDate;
    }

    public String getCollectionType() {
        return labWorks.getClass().getName();
    }

    public int getSize() {
        return labWorks.size();
    }

    public void add(LabWork labWork) {
        labWork.setId(generateNextId());
        labWork.setCreationDate(ZonedDateTime.now());
        labWorks.add(labWork);
    }

    public boolean update(long id, LabWork newLabWork) {
        return labWorks.stream()
                .filter(labWork -> labWork.getId() == id)
                .findFirst()
                .map(labWork -> {
                    newLabWork.setId(id);
                    newLabWork.setCreationDate(labWork.getCreationDate());
                    labWorks.set(labWorks.indexOf(labWork), newLabWork);
                    return true;
                })
                .orElse(false);
    }

    public boolean removeById(long id) {
        return labWorks.removeIf(labWork -> labWork.getId() == id);
    }

    public void clear() {
        labWorks.clear();
    }

    public List<LabWork> getAll() {
        return labWorks.stream()
                .sorted(Comparator.comparing(LabWork::getName))
                .collect(Collectors.toList());
    }

    public void shuffle() {
        labWorks = new Vector<>(labWorks.stream()
                .sorted((a, b) -> (int) (Math.random() * 3 - 1))
                .collect(Collectors.toList()));
    }

    public void reorder() {
        labWorks = new Vector<>(labWorks.stream()
                .sorted(Comparator.comparing(LabWork::getName).reversed())
                .collect(Collectors.toList()));
    }

    public void sort() {
        labWorks = new Vector<>(labWorks.stream()
                .sorted(Comparator.comparingLong(LabWork::getId))
                .collect(Collectors.toList()));
    }

    public void removeAllByDifficulty(Difficulty difficulty) {
        labWorks.removeIf(labWork -> labWork.getDifficulty() == difficulty);
    }

    public boolean removeAnyByDifficulty(Difficulty difficulty) {
        labWorks.stream()
                .filter(labWork -> labWork.getDifficulty() == difficulty)
                .findFirst()
                .ifPresent(labWork -> labWorks.remove(labWork));
        return false;
    }

    public List<LabWork> filterGreaterThanMinimalPoint(Long minimalPoint) {
        return labWorks.stream()
                .filter(labWork -> labWork.getMinimalPoint() != null && labWork.getMinimalPoint() > minimalPoint)
                .sorted(Comparator.comparing(LabWork::getName))
                .collect(Collectors.toList());
    }

    public long generateNextId() {
        return labWorks.isEmpty() ? 1 : labWorks.stream()
                .mapToLong(LabWork::getId)
                .max()
                .orElse(0) + 1;
    }

    public boolean containsId(long id) {
        return labWorks.stream().anyMatch(labWork -> labWork.getId() == id);
    }

    public void loadData(List<LabWork> data) {
        labWorks.addAll(data);
    }
}
