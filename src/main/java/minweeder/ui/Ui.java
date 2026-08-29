package minweeder.ui;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import minweeder.task.Task;
import minweeder.task.TaskList;

/**
 * Handles all console input and output for the application, including
 * formatted messages, banners, and prompts shown to the user.
 */
public class Ui {
    private static final String LINE =
            "────────────────────────────────────────────────────────────────\n";
    private static final String BANNER = " __  __ ___ _   ___        _______ _____ ____  _____ ____  \n"
            + "|  \\/  |_ _| \\ | \\ \\      / / ____| ____|  _ \\| ____|  _ \\ \n"
            + "| |\\/| || ||  \\| |\\ \\ /\\ / /|  _| |  _| | | | |  _| | |_) |\n"
            + "| |  | || || |\\  | \\ V  V / | |___| |___| |_| | |___|  _ < \n"
            + "|_|  |_|___|_| \\_|  \\_/\\_/  |_____|_____|____/|_____|_| \\_\\\n";
    private static final String GREETING = "Heyyo I'm Minweeder!\nLETS GET THINGS DONE RAHH";
    private static final String GOODBYE = "Goodbye! Hope you had a productive session :)";
    private static final DateTimeFormatter QUERY_DATE_DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("MMM dd yyyy");

    private final Scanner scanner = new Scanner(System.in);

    /**
     * Prints one or more messages, each on its own line, surrounded above and below
     * by a horizontal divider. Used by all the show* methods to keep output consistent.
     *
     * @param messages the lines to print, in order.
     */
    private void printBlock(String... messages) {
        System.out.print(LINE);
        for (String message : messages) {
            System.out.println(message);
        }
        System.out.print(LINE);
    }

    /**
     * Prints the welcome banner and greeting shown when Minweeder starts.
     */
    public void showWelcome() {
        printBlock(BANNER, GREETING);
    }

    /**
     * Prints the goodbye message shown when the user exits.
     */
    public void showGoodbye() {
        printBlock(GOODBYE);
    }

    /**
     * Reads and returns the next line of user input, with leading/trailing whitespace trimmed.
     *
     * @return the trimmed line of input.
     */
    public String readCommand() {
        return scanner.nextLine().trim();
    }

    /**
     * Closes the input scanner. Should be called once, when the program is shutting down.
     */
    public void close() {
        scanner.close();
    }

    /**
     * Prints a message explaining that the save file could not be read.
     *
     * @param message details of the underlying error.
     */
    public void showLoadingError(String message) {
        printBlock("I couldn't read your saved tasks, so let's start afresh. " + message);
    }

    /**
     * Informs the user that some lines in the save file could not be read.
     *
     * @param skippedLineCount the number of lines that were skipped.
     */
    public void showSkippedLines(int skippedLineCount) {
        printBlock("BTW " + skippedLineCount
                + " line(s) of your save file were unreadable so some may be missing :(");
    }

    /**
     * Prints an error message in response to an invalid command.
     *
     * @param message the error message to show.
     */
    public void showError(String message) {
        printBlock("Erm...you can't do that..." + message);
    }

    /**
     * Confirms that a task was added to the list.
     *
     * @param label a human-readable name for the task type, e.g. "Todo".
     * @param task the task that was added.
     * @param totalTasks the total number of tasks now in the list.
     */
    public void showTaskAdded(String label, Task task, int totalTasks) {
        printBlock("Okay! " + label + " successfully added:",
                "  " + task,
                "Now you have " + totalTasks + " tasks in your list.");
    }

    /**
     * Confirms that a task was removed from the list.
     *
     * @param task the task that was removed.
     * @param totalTasks the total number of tasks remaining in the list.
     */
    public void showTaskDeleted(Task task, int totalTasks) {
        printBlock("Task successfully removed: ",
                " " + task,
                "Now you have " + totalTasks + " tasks in your list.");
    }

    /**
     * Confirms that a task was marked as done.
     *
     * @param task the task that was marked.
     */
    public void showTaskMarked(Task task) {
        printBlock("Congrats! Task has been marked as completed:",
                "  " + task);
    }

    /**
     * Confirms that a task was marked as not done.
     *
     * @param task the task that was unmarked.
     */
    public void showTaskUnmarked(Task task) {
        printBlock("Done! Task has been marked as not done yet:",
                "  " + task);
    }

    /**
     * Displays every task currently in the list.
     *
     * @param tasks the list of tasks to display.
     */
    public void showList(TaskList tasks) {
        String[] listing = new String[tasks.size() + 1];
        listing[0] = "Here are your tasks:";
        for (int i = 0; i < tasks.size(); i++) {
            listing[i + 1] = (i + 1) + ". " + tasks.get(i);
        }
        printBlock(listing);
    }

    /**
     * Displays only the tasks that occur on a given date.
     *
     * @param date the date to filter tasks by.
     * @param tasks the list of tasks to search.
     */
    public void showTasksOn(LocalDate date, TaskList tasks) {
        ArrayList<String> matches = new ArrayList<>();
        matches.add("Tasks occurring on " + date.format(QUERY_DATE_DISPLAY_FORMAT) + ":");
        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).isOccurringOn(date)) {
                matches.add((i + 1) + ". " + tasks.get(i));
            }
        }
        printBlock(matches.toArray(new String[0]));
    }

    /**
     * Prints the tasks that matched a find query, numbered by their position in the
     * full list so that the numbers shown can be used with commands such as mark.
     *
     * @param matchingIndices the zero-based indices of the tasks to display.
     * @param tasks the full task list the indices refer to.
     */
    public void showFoundTasks(List<Integer> matchingIndices, TaskList tasks) {
        String[] listing = new String[matchingIndices.size() + 1];
        listing[0] = "Here are the matching tasks in your list:";
        for (int i = 0; i < matchingIndices.size(); i++) {
            int index = matchingIndices.get(i);
            listing[i + 1] = (index + 1) + ". " + tasks.get(index);
        }
        printBlock(listing);
    }
}
