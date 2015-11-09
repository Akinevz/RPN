/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reversepolishparser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

/**
 *
 * @author zugbug
 */
public class ReversePolishParser {

    static String[] comments = {"#", "//", "?"};
    private static boolean running;

    static String readFile(String filename) throws FileNotFoundException { // Leave as stream
        Scanner scan = new Scanner(new File(filename)).useDelimiter("\n");
        return StreamSupport.stream(((Iterable<String>) () -> scan).spliterator(), false)
                .filter(s -> !s.equalsIgnoreCase(""))
                .filter(stripComments())
                .collect(StringBuilder::new, (sb, str) -> sb.append(str).append('\n'), StringBuilder::append)
                .reverse().deleteCharAt(0).reverse().toString().trim();
    }

    static void help() {
        System.out.println("\nEnter file name, to parse file.\n"
                + "`Return` key to enter the equation by hand.\n"
                + "Type `--` to exit.");
    }

    public static void main(String[] args){
        running = true;
        help();
        while (running) {
            try {

                String decision = askForInput("", new String[]{}).trim();
                String input;
                switch (decision.trim()) {
                    case "help":
                    case "?":
                        help();
                        continue;
                    case "--":
                        running = false;
                        return;
                    case "":
                        input = askForInput("Enter a valid RPN:");
                        break;
                    default:
                        input = readFile(decision);
                        break;
                }
                System.out.println("Input: [" + input + "]");
                Tokeniser processor
                        = new Tokeniser(input, " ");
                processor.tryParse();
                processor.processTokens();
                System.out.println("Output: " + processor);
                System.out.println("\n-------------Again?--------------\n");
            } catch (FileNotFoundException | Tokeniser.FMTException e) {
                System.out.println("\n\tERROR: \""+e.getMessage()+"\"");
                System.out.println("Please enter again.\n");
            }
        }
    }

    private static boolean matchAny(String[] target, Predicate<? super String> match) {
        for (String par : target) {
            if (match.test(par)) {
                return true;
            }
        }
        return false;
    }

    private static Predicate<? super String> stripComments() {
        return ((Predicate<? super String>) (line -> matchAny(comments, (s) -> line.startsWith(s)))).negate();
    }

    public static boolean matchEither(String tar, String[] matches) {
        return matchAny(matches, (s) -> tar.equalsIgnoreCase(s));
    }

    public static String askForInput(String question, String[] invalid) {
        Scanner user = new Scanner(System.in);
        String[] wrong = invalid;
        String input;
        do {
            System.out.println("\t" + question);
            input = user.nextLine();
        } while (matchEither(input, wrong));
        return input;
    }

    public static String askForInput(String question) {
        return askForInput(question, new String[]{"", null});
    }

}
