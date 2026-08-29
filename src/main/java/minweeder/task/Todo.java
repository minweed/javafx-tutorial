package minweeder.task;

/**
 * A simple task with no associated date or time.
 */
public class Todo extends Task {
    /**
     * Creates a todo with the given description.
     *
     * @param description the description of the todo.
     */
    public Todo(String description) {
        super(description);
    }

    @Override
    public String toFileString() {
        return "T | " + super.toFileFields();
    }

    @Override
    public String toString() {
        return "[T]" + super.toString();
    }
}
