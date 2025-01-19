package org.poo.main.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.main.tools.Tools;
import org.poo.main.user.User;
import org.poo.fileio.CommandInput;

import java.util.List;

public final class PrintUsersCommand implements Command {
    private final ObjectMapper objectMapper;
    private final List<User> users;

    public PrintUsersCommand(final ObjectMapper objectMapper, final List<User> users) {
        this.objectMapper = objectMapper;
        this.users = users;
    }

    @Override
    public void execute(final CommandInput command, final ArrayNode output) {
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
