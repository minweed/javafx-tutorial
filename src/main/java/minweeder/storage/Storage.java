package minweeder.storage;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import minweeder.exception.MinweederException;
import minweeder.task.Deadline;
import minweeder.task.Event;
import minweeder.task.Task;
import minweeder.task.TaskList;
import minweeder.task.Todo;

/**
 * Persists tasks to, and loads them from, a save file on disk.
 */
public class Storage {
    private static final Path FILE_PATH = Paths.get("data", "minweeder.txt");

    private int skippedLineCount = 0;

    /**
     * Writes every task in the list to the save file, overwriting its previous contents.
     *
     * @param tasks the tasks to save.
     * @throws MinweederException if the save file could not be written.
     */
    public void save(TaskList tasks) throws MinweederException {
        try {
            Files.createDirectories(FILE_PATH.getParent());

            try (BufferedWriter writer = Files.newBufferedWriter(FILE_PATH)) {
                for (int i = 0; i < tasks.size(); i++) {
                    writer.write(tasks.get(i).toFileString());
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            throw new MinweederException("I couldn't save your tasks: " + e.getMessage());
        }
    }

    /**
     * Reads tasks from the save file, if it exists. Lines that cannot be
     * parsed are skipped and counted, retrievable via {@link #getSkippedLineCount()}.
     *
     * @return the loaded tasks, or an empty list if no save file exists.
     * @throws MinweederException if the save file could not be read.
     */
    public TaskList load() throws MinweederException {
        TaskList tasks = new TaskList();
        skippedLineCount = 0;
        if (!Files.exists(FILE_PATH)) {
            return tasks;
        }
        try {
            for (String line : Files.readAllLines(FILE_PATH)) {
                if (line.isBlank()) {
                    continue;
                }
                Task task = parseTask(line);
                if (task == null) {
                    skippedLineCount++;
                } else {
                    tasks.add(task);
                }
            }
        } catch (IOException e) {
            throw new MinweederException("I couldn't read your saved tasks: " + e.getMessage());
        }
        return tasks;
    }

    /**
     * Returns the number of lines skipped during the most recent {@link #load()}
     * because they could not be parsed into a task.
     *
     * @return the number of skipped lines.
     */
    public int getSkippedLineCount() {
        return skippedLineCount;
    }

    /**
     * Parses a single line of the storage file into a {@link Task}.
     *
     * @param line a "|"-separated line read from the storage file.
     * @return the parsed task, or null if the line is malformed and should be skipped.
     */
    private static Task parseTask(String line) {
        String[] parts = line.split("\\|");
        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();
        }
        if (parts.length < 3 || parts[2].isEmpty()) {
            return null;
        }
        Task task;
        switch (parts[0]) {
            case "T":
                task = new Todo(parts[2]);
                break;
            case "D":
                if (parts.length < 4 || parts[3].isEmpty()) {
                    return null;
                }
                LocalDateTime by;
                try {
                    by = LocalDateTime.parse(parts[3]);
                } catch (DateTimeParseException e) {
                    return null;
                }
                task = new Deadline(parts[2], by);
                break;
            case "E":
                if (parts.length < 5 || parts[3].isEmpty() || parts[4].isEmpty()) {
                    return null;
                }
                task = new Event(parts[2], parts[3], parts[4]);
                break;
            default:
                return null;
        }
        if (parts[1].equals("1")) {
            task.mark();
        } else if (!parts[1].equals("0")) {
            return null;
        }
        return task;
    }
}
