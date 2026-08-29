import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import minweeder.command.CommandWord;
import minweeder.exception.MinweederException;
import minweeder.parser.Parser;
import minweeder.storage.Storage;
import minweeder.task.Deadline;
import minweeder.task.Event;
import minweeder.task.Task;
import minweeder.task.TaskList;
import minweeder.task.Todo;

/**
 * GUI-facing entry point that dispatches parsed user input to the Minweeder
 * task operations (todo/deadline/event/list/mark/unmark/delete/on/find/bye),
 * returning each result as a String for display in the chat window.
 */
public class Duke {
    private static final DateTimeFormatter QUERY_DATE_DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("MMM dd yyyy");

    private final Storage storage;
    private final TaskList tasks;
    private String commandType;

    public static void main(String[] args) {
        System.out.println("Hello!");
    }

    public Duke() {
        storage = new Storage();
        TaskList loaded;
        try {
            loaded = storage.load();
        } catch (MinweederException e) {
            loaded = new TaskList();
        }
        tasks = loaded;
    }

    /**
     * Generates a response for the user's chat message.
     */
    public String getResponse(String input) {
        commandType = null;
        try {
            String[] breakdown = Parser.splitCommand(input);
            CommandWord commandWord = Parser.parseCommandWord(breakdown);

            switch (commandWord) {
            case BYE:
                return "Goodbye! Hope you had a productive session :)";
            case LIST:
                return formatList();
            case MARK: {
                int index = Parser.parseIndex(breakdown, tasks);
                tasks.get(index).mark();
                storage.save(tasks);
                commandType = "ChangeMarkCommand";
                return "Congrats! Task has been marked as completed:\n  " + tasks.get(index);
            }
            case UNMARK: {
                int index = Parser.parseIndex(breakdown, tasks);
                tasks.get(index).unmark();
                storage.save(tasks);
                commandType = "ChangeMarkCommand";
                return "Done! Task has been marked as not done yet:\n  " + tasks.get(index);
            }
            case TODO: {
                String description = Parser.requireArguments(breakdown, "todo", "todo read book");
                return addTask("TODO", new Todo(description));
            }
            case DEADLINE: {
                String example = "deadline return book /by 2/12/2019 1800";
                String arguments = Parser.requireArguments(breakdown, "deadline", example);
                String[] parts = Parser.requireKeyword(arguments, "/by", example);
                LocalDateTime by = Parser.parseDeadlineBy(parts[1], example);
                return addTask("Deadline", new Deadline(parts[0], by));
            }
            case EVENT: {
                String example = "event project meeting /from Mon 2pm /to 4pm";
                String arguments = Parser.requireArguments(breakdown, "event", example);
                String[] fromParts = Parser.requireKeyword(arguments, "/from", example);
                String[] toParts = Parser.requireKeyword(fromParts[1], "/to", example);
                return addTask("Event", new Event(fromParts[0], toParts[0], toParts[1]));
            }
            case DELETE: {
                int index = Parser.parseIndex(breakdown, tasks);
                Task deleted = tasks.delete(index);
                storage.save(tasks);
                commandType = "DeleteCommand";
                return "Task successfully removed:\n  " + deleted
                        + "\nNow you have " + tasks.size() + " tasks in your list.";
            }
            case ON: {
                String example = "on 2/12/2019";
                String argument = Parser.requireArguments(breakdown, "on", example);
                LocalDate date = Parser.parseOnDate(argument, example);
                return formatTasksOn(date);
            }
            case FIND: {
                String example = "find book";
                String keyword = Parser.requireArguments(breakdown, "find", example);
                return formatFoundTasks(tasks.findIndices(keyword));
            }
            default:
                // Unreachable: CommandWord.getCommandWord() only returns known enum values.
                return "";
            }
        } catch (MinweederException e) {
            return "Erm...you can't do that..." + e.getMessage();
        }
    }

    /**
     * Returns the type of the last command processed, e.g. "AddCommand", "ChangeMarkCommand", "DeleteCommand".
     */
    public String getCommandType() {
        return commandType;
    }

    private String addTask(String label, Task task) throws MinweederException {
        tasks.add(task);
        storage.save(tasks);
        commandType = "AddCommand";
        return "Okay! " + label + " successfully added:\n  " + task
                + "\nNow you have " + tasks.size() + " tasks in your list.";
    }

    private String formatList() {
        if (tasks.size() == 0) {
            return "Here are your tasks:\n(no tasks yet)";
        }
        StringBuilder sb = new StringBuilder("Here are your tasks:");
        for (int i = 0; i < tasks.size(); i++) {
            sb.append("\n").append(i + 1).append(". ").append(tasks.get(i));
        }
        return sb.toString();
    }

    private String formatTasksOn(LocalDate date) {
        StringBuilder sb = new StringBuilder(
                "Tasks occurring on " + date.format(QUERY_DATE_DISPLAY_FORMAT) + ":");
        boolean hasMatch = false;
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).isOccurringOn(date)) {
                sb.append("\n").append(i + 1).append(". ").append(tasks.get(i));
                hasMatch = true;
            }
        }
        if (!hasMatch) {
            sb.append("\n(none)");
        }
        return sb.toString();
    }

    private String formatFoundTasks(List<Integer> matchingIndices) {
        if (matchingIndices.isEmpty()) {
            return "Here are the matching tasks in your list:\n(none found)";
        }
        StringBuilder sb = new StringBuilder("Here are the matching tasks in your list:");
        for (int index : matchingIndices) {
            sb.append("\n").append(index + 1).append(". ").append(tasks.get(index));
        }
        return sb.toString();
    }
}
