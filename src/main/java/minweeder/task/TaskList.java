package minweeder.task;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple ordered collection of {@link Task}s.
 */
public class TaskList {
    private final ArrayList<Task> tasks = new ArrayList<>();

    /**
     * Adds a task to the end of the list.
     *
     * @param task the task to add.
     */
    public void add(Task task) {
        tasks.add(task);
    }

    /**
     * Returns the task at the given index.
     *
     * @param index the zero-based index of the task.
     * @return the task at that index.
     */
    public Task get(int index) {
        return tasks.get(index);
    }

    /**
     * Returns the number of tasks in the list.
     *
     * @return the number of tasks.
     */
    public int size() {
        return tasks.size();
    }

    /**
     * Removes and returns the task at the given index.
     *
     * @param index the zero-based index of the task to remove.
     * @return the removed task.
     */
    public Task delete(int index) {
        return tasks.remove(index);
    }

    /**
     * Finds the positions of tasks whose description contains the given keyword.
     * Indices, rather than the tasks themselves, are returned so that callers can
     * refer to each match by its number in the full list.
     *
     * @param keyword the keyword to search for, matched case-insensitively.
     * @return the zero-based indices of the matching tasks, in list order.
     */
    public List<Integer> findIndices(String keyword) {
        List<Integer> matchingIndices = new ArrayList<>();
        String lowerKeyword = keyword.toLowerCase();
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).getDescription().toLowerCase().contains(lowerKeyword)) {
                matchingIndices.add(i);
            }
        }
        return matchingIndices;
    }
}
