/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reversepolishparser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;
import com.akinevz.utils.Chance;
import com.akinevz.utils.Logic;

/**
 *
 * @author zugbug
 */
public class ReversePolishParser {

    static String[] comments = {"#", "//", "?"};
    private static boolean running;

   

    static void help() {
        System.out.println("\nInput normally to parse.\n"
                + "`Return` key to read from a file.\n"
                + "Type `--` to exit.");
    }

    public static void main(String[] args) throws FileNotFoundException {
        running = true;
        help();
        while (running) {
            try {
                String input;
                String decision = askForInput("", new String[]{}).trim();
                switch (decision) {
                    case "help":
                    case "?":
                        help();
                        continue;
                    case "--":
                    case "quit":
                    case "q":
                        running = false;
                        return;
                    case "":
                        input = com.akinevz.utils.EasyFile.read(new File(query("Enter name for file containing RPN:")));
                        break;
                    default:
                        input = decision;
                        break;
                }

                System.out.println("Input: [" + input + "]");
                Tokeniser processor
                        = new Tokeniser(input, " ");
                processor.tryParse();
                processor.processTokens();
                System.out.println("Output: " + processor);
                System.out.println("\n-------------Again?--------------\n");
            } catch (Tokeniser.FMTException e) {
                System.out.println("\n\tERROR: \"" + e.getMessage() + "\"");
                help();
            }
        }
    }


    private static Predicate<? super String> stripComments() {
        return ((Predicate<String>) (line -> Logic.matchAny(line::startsWith, comments))).negate();
    }


    public static String askForInput(String question, String[] invalid) {
        Scanner user = new Scanner(System.in);
        String input;
        do {
            System.out.println("\t" + question);
            input = user.nextLine();
        } while (Logic.matchEither(input, invalid));
        return input;
    }

    public static String query(String question) {
        return askForInput(question, new String[]{"", null});
    }

}
