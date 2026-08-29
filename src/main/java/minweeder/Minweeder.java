package minweeder;

import java.time.LocalDate;
import java.time.LocalDateTime;

import minweeder.command.CommandWord;
import minweeder.exception.MinweederException;
import minweeder.parser.Parser;
import minweeder.storage.Storage;
import minweeder.task.Deadline;
import minweeder.task.Event;
import minweeder.task.Task;
import minweeder.task.TaskList;
import minweeder.task.Todo;
import minweeder.ui.Ui;

/**
 * Entry point for the Minweeder task-tracking application.
 * Reads user commands from the console in a loop, dispatching each to the
 * appropriate task operation until the user exits.
 */
public class Minweeder {
    /**
     * Adds a task to the list, persists the updated list to storage, and shows a
     * confirmation message. Shared by the todo/deadline/event command handlers.
     *
     * @param tasks the task list to add to.
     * @param storage the storage used to persist the updated list.
     * @param ui the UI used to show the confirmation message.
     * @param label a human-readable name for the task type, e.g. "TODO".
     * @param task the task to add.
     * @throws MinweederException if saving the updated list fails.
     */
    private static void addTask(TaskList tasks, Storage storage, Ui ui, String label, Task task)
            throws MinweederException {
        tasks.add(task);
        storage.save(tasks);
        ui.showTaskAdded(label, task, tasks.size());
    }

    /**
     * Starts Minweeder: loads any saved tasks, then repeatedly reads and executes
     * user commands until told to exit.
     *
     * @param args unused command-line arguments.
     */
    public static void main(String[] args) {
        Ui ui = new Ui();
        ui.showWelcome();

        Storage storage = new Storage();
        TaskList tasks = new TaskList();
        try {
            tasks = storage.load();
            if (storage.getSkippedLineCount() > 0) {
                ui.showSkippedLines(storage.getSkippedLineCount());
            }
        } catch (MinweederException e) {
            ui.showLoadingError(e.getMessage());
        }

        boolean isRunning = true;
        while (isRunning) {
            String command = ui.readCommand();
            if (command.isEmpty()) {
                continue;
            }
            try {
                String[] breakdown = Parser.splitCommand(command);
                CommandWord commandWord = Parser.parseCommandWord(breakdown);

                switch (commandWord) {
                    case BYE:
                        ui.showGoodbye();
                        isRunning = false;
                        break;
                    case LIST:
                        ui.showList(tasks);
                        break;
                    case MARK: {
                        int index = Parser.parseIndex(breakdown, tasks);
                        tasks.get(index).mark();
                        storage.save(tasks);
                        ui.showTaskMarked(tasks.get(index));
                        break;
                    }
                    case UNMARK: {
                        int index = Parser.parseIndex(breakdown, tasks);
                        tasks.get(index).unmark();
                        storage.save(tasks);
                        ui.showTaskUnmarked(tasks.get(index));
                        break;
                    }
                    case TODO: {
                        String description = Parser.requireArguments(breakdown, "todo", "todo read book");
                        Todo todo = new Todo(description);
                        addTask(tasks, storage, ui, "TODO", todo);
                        break;
                    }
                    case DEADLINE: {
                        String example = "deadline return book /by 2/12/2019 1800";
                        String arguments = Parser.requireArguments(breakdown, "deadline", example);
                        String[] parts = Parser.requireKeyword(arguments, "/by", example);
                        LocalDateTime by = Parser.parseDeadlineBy(parts[1], example);
                        Deadline deadline = new Deadline(parts[0], by);
                        addTask(tasks, storage, ui, "Deadline", deadline);
                        break;
                    }
                    case EVENT: {
                        String example = "event project meeting /from Mon 2pm /to 4pm";
                        String arguments = Parser.requireArguments(breakdown, "event", example);
                        String[] fromParts = Parser.requireKeyword(arguments, "/from", example);
                        String[] toParts = Parser.requireKeyword(fromParts[1], "/to", example);
                        Event event = new Event(fromParts[0], toParts[0], toParts[1]);
                        addTask(tasks, storage, ui, "Event", event);
                        break;
                    }
                    case DELETE: {
                        int index = Parser.parseIndex(breakdown, tasks);
                        Task deleted = tasks.delete(index);
                        storage.save(tasks);
                        ui.showTaskDeleted(deleted, tasks.size());
                        break;
                    }
                    case ON: {
                        String example = "on 2/12/2019";
                        String argument = Parser.requireArguments(breakdown, "on", example);
                        LocalDate date = Parser.parseOnDate(argument, example);
                        ui.showTasksOn(date, tasks);
                        break;
                    }
                    case FIND: {
                        String example = "find book";
                        String keyword = Parser.requireArguments(breakdown, "find", example);
                        ui.showFoundTasks(tasks.findIndices(keyword), tasks);
                        break;
                    }
                    default:
                        // Unreachable: CommandWord.getCommandWord() only returns known enum values.
                        break;
                }
            } catch (MinweederException e) {
                ui.showError(e.getMessage());
            }
        }
        ui.close();
    }
}
