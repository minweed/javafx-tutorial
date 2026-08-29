package minweeder.parser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import minweeder.command.CommandWord;
import minweeder.exception.MinweederException;
import minweeder.task.TaskList;

/**
 * Parses raw user input into commands and arguments that the application
 * can act on, throwing {@link MinweederException} on invalid input.
 */
public class Parser {
    private static final DateTimeFormatter DEADLINE_INPUT_FORMAT =
            DateTimeFormatter.ofPattern("d/M/yyyy HHmm");
    private static final DateTimeFormatter QUERY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("d/M/yyyy");

    /**
     * Splits a line of user input into the command word and its remaining arguments.
     *
     * @param command the raw input line.
     * @return an array of at most two elements: the command word, and the rest of the line.
     */
    public static String[] splitCommand(String command) {
        return command.split(" ", 2);
    }

    /**
     * Resolves the command word from a split command.
     *
     * @param breakdown the result of {@link #splitCommand(String)}.
     * @return the recognized command word.
     * @throws MinweederException if the first token is not a known command.
     */
    public static CommandWord parseCommandWord(String[] breakdown) throws MinweederException {
        return CommandWord.getCommandWord(breakdown[0]);
    }

    /**
     * Extracts the argument text for a command, requiring it to be non-blank.
     *
     * @param breakdown the result of {@link #splitCommand(String)}.
     * @param commandWord the name of the command, used in the error message.
     * @param example an example of valid usage, shown in the error message.
     * @return the trimmed argument text.
     * @throws MinweederException if no argument text was supplied.
     */
    public static String requireArguments(String[] breakdown, String commandWord, String example)
            throws MinweederException {
        if (breakdown.length < 2 || breakdown[1].isBlank()) {
            throw new MinweederException("a " + commandWord + " needs a description. e.g. "
                    + example);
        }
        return breakdown[1].trim();
    }

    /**
     * Splits text around a required keyword, such as {@code /by} or {@code /from}.
     *
     * @param text the text to split.
     * @param keyword the keyword that must separate the two halves.
     * @param example an example of valid usage, shown in the error message.
     * @return a two-element array of the trimmed text before and after the keyword.
     * @throws MinweederException if the keyword is missing or either side is blank.
     */
    public static String[] requireKeyword(String text, String keyword, String example)
            throws MinweederException {
        String[] parts = text.split(" " + keyword + " ", 2);
        if (parts.length < 2 || parts[0].isBlank() || parts[1].isBlank()) {
            throw new MinweederException("You need something on either side of " + keyword
                    + ". e.g. " + example);
        }
        return new String[] {parts[0].trim(), parts[1].trim()};
    }

    /**
     * Parses a 1-based task number from user input and converts it to a valid
     * 0-based index into the task list.
     *
     * @param breakdown the result of {@link #splitCommand(String)}.
     * @param tasks the task list the index will be used against.
     * @return the 0-based index of the referenced task.
     * @throws MinweederException if no number is given, it isn't a number,
     *     the list is empty, or the number is out of range.
     */
    public static int parseIndex(String[] breakdown, TaskList tasks) throws MinweederException {
        if (breakdown.length < 2 || breakdown[1].isBlank()) {
            throw new MinweederException("Which task? Choose a number, e.g. "
                    + breakdown[0] + " 2");
        }
        String argument = breakdown[1].trim();
        int taskNumber;
        try {
            taskNumber = Integer.parseInt(argument);
        } catch (NumberFormatException e) {
            throw new MinweederException("'" + argument + "' is not a number!");
        }
        if (tasks.size() == 0) {
            throw new MinweederException("your list is empty, so there is nothing to "
                    + breakdown[0] + " yet.");
        }
        if (taskNumber < 1 || taskNumber > tasks.size()) {
            throw new MinweederException("you have " + tasks.size()
                    + " tasks, so pick a number from 1 to " + tasks.size() + ".");
        }
        return taskNumber - 1;
    }

    /**
     * Parses the {@code /by} date-time argument for a deadline.
     *
     * @param text the text to parse, expected in {@code d/M/yyyy HHmm} format.
     * @param example an example of valid usage, shown in the error message.
     * @return the parsed date-time.
     * @throws MinweederException if the text does not match the expected format.
     */
    public static LocalDateTime parseDeadlineBy(String text, String example) throws MinweederException {
        try {
            return LocalDateTime.parse(text, DEADLINE_INPUT_FORMAT);
        } catch (DateTimeParseException e) {
            throw new MinweederException("please use d/M/yyyy HHmm for the date, e.g. " + example);
        }
    }

    /**
     * Parses the date argument for the {@code on} command.
     *
     * @param text the text to parse, expected in {@code d/M/yyyy} format.
     * @param example an example of valid usage, shown in the error message.
     * @return the parsed date.
     * @throws MinweederException if the text does not match the expected format.
     */
    public static LocalDate parseOnDate(String text, String example) throws MinweederException {
        try {
            return LocalDate.parse(text, QUERY_DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new MinweederException("please use d/M/yyyy for the date, e.g. " + example);
        }
    }
}
