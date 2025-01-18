package org.poo.main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.main.tools.Tools;
import org.poo.main.user.User;
import org.poo.fileio.CommandInput;

import java.util.List;

/**
 * Command to print all users in the bank system.
 */
public class PrintUsersCommand implements Command {
    private final ObjectMapper objectMapper;
    private final List<User> users;

    /**
     * Constructs a PrintUsersCommand with the necessary dependencies.
     *
     * @param objectMapper the ObjectMapper instance for JSON operations
     * @param users        the list of users in the bank system
     */
    public PrintUsersCommand(ObjectMapper objectMapper, List<User> users) {
        this.objectMapper = objectMapper;
        this.users = users;
    }

    @Override
    public void execute(CommandInput command, ArrayNode output) {
        // Create a node as the command result
        ObjectNode commandResultNode = objectMapper.createObjectNode();
        commandResultNode.put("command", "printUsers");

        // Create an array to hold the users
        ArrayNode usersArray = objectMapper.createArrayNode();

        // Iterate over all users in the bank system to print each user's info
        for (User user : users) {
            ObjectNode userNode = Tools.printUser(user);
            usersArray.add(userNode);
        }

        commandResultNode.set("output", usersArray);
        commandResultNode.put("timestamp", command.getTimestamp());

        // Add the result node to the output
        output.add(commandResultNode);
    }
}
