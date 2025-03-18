package org.poo.main.commands;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.fileio.CommandInput;

/**
 * Command interface for all bank system commands.
 */
public interface Command {
    /**
     * Executes the command with the given input and appends the result to the output array.
     *
     * @param command the input command data
     * @param output  the JSON array to append the result
     */
    void execute(CommandInput command, ArrayNode output);
}
