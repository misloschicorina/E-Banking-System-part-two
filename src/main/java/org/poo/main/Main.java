package org.poo.main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.poo.checker.Checker;
import org.poo.checker.CheckerConstants;
import org.poo.fileio.ObjectInput;
import org.poo.main.Commerciant.Commerciant;
import org.poo.main.bank.BankSystem;
import org.poo.main.exchange_rate.ExchangeRate;
import org.poo.main.user.User;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Objects;

/**
 * The entry point to this homework. It runs the checker that tests your implementation.
 */
public final class Main {
    /**
     * for coding style
     */
    private Main() {
    }

    /**
     * DO NOT MODIFY MAIN METHOD
     * Call the checker
     * @param args from command line
     * @throws IOException in case of exceptions to reading / writing
     */
    public static void main(final String[] args) throws IOException {
        File directory = new File(CheckerConstants.TESTS_PATH);
        Path path = Paths.get(CheckerConstants.RESULT_PATH);

        if (Files.exists(path)) {
            File resultFile = new File(String.valueOf(path));
            for (File file : Objects.requireNonNull(resultFile.listFiles())) {
                file.delete();
            }
            resultFile.delete();
        }
        Files.createDirectories(path);

        var sortedFiles = Arrays.stream(Objects.requireNonNull(directory.listFiles())).
                sorted(Comparator.comparingInt(Main::fileConsumer))
                .toList();

        for (File file : sortedFiles) {
            String filepath = CheckerConstants.OUT_PATH + file.getName();
            File out = new File(filepath);
            boolean isCreated = out.createNewFile();
            if (isCreated) {
                action(file.getName(), filepath);
            }
        }

        Checker.calculateScore();
    }

    /**
     * @param filePath1 for input file
     * @param filePath2 for output file
     * @throws IOException in case of exceptions to reading / writing
     */
    public static void action(final String filePath1,
                              final String filePath2) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(CheckerConstants.TESTS_PATH + filePath1);
        ObjectInput inputData = objectMapper.readValue(file, ObjectInput.class);

        ArrayNode output = objectMapper.createArrayNode();

        BankSystem bankSystem = new BankSystem();

        // Parsing the users from input and adding them to the bank system
        for (var userInput : inputData.getUsers()) {
            User user = new User(
                    userInput.getFirstName(),
                    userInput.getLastName(),
                    userInput.getEmail(),
                    userInput.getBirthDate(),
                    userInput.getOccupation());
            bankSystem.addUser(user);
        }

        // Parsing the exchange rates from input and adding them to the bank system
        for (var exchangeRateInput : inputData.getExchangeRates()) {
            ExchangeRate exchangeRate = new ExchangeRate(
                    exchangeRateInput.getFrom(),
                    exchangeRateInput.getTo(),
                    exchangeRateInput.getRate()
            );
            bankSystem.addExchangeRate(exchangeRate);
        }

        // Parsing the merchants from input and adding them to the bank system
        for (var commerciantInput : inputData.getCommerciants()) {
            Commerciant commerciant = new Commerciant(
                    commerciantInput.getCommerciant(),
                    commerciantInput.getId(),
                    commerciantInput.getAccount(),
                    commerciantInput.getType(),
                    commerciantInput.getCashbackStrategy()
            );
            bankSystem.addCommerciant(commerciant);
        }

        // Process the commands from the input and generate the output
        bankSystem.processCommands(inputData.getCommands(), output);

        ObjectWriter objectWriter = objectMapper.writerWithDefaultPrettyPrinter();
        objectWriter.writeValue(new File(filePath2), output);
    }

    /**
     * Method used for extracting the test number from the file name.
     *
     * @param file the input file
     * @return the extracted numbers
     */
    public static int fileConsumer(final File file) {
        String fileName = file.getName()
                .replaceAll(CheckerConstants.DIGIT_REGEX, CheckerConstants.EMPTY_STR);
        return Integer.parseInt(fileName.substring(0, 2));
    }
}
