/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reversepolishparser;

import com.akinevz.utils.Tuple;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.EmptyStackException;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Scanner;
import java.util.function.BinaryOperator;
import java.util.regex.Pattern;

/**
 *
 * @author zugbug
 */
class Tokeniser implements Iterable<Token> {

    @Override
    public String toString() {
        return stack.toString();
    }

    private final Scanner scanner;
    Queue<Token> tokens = new ArrayDeque<>();

    int position;

    Tokeniser(String lines) {
        this(lines, " ");
    }

    Tokeniser(String lines, String delim) {
        this.scanner = new Scanner(lines)
                .useDelimiter(Pattern.compile(delim + "|\n"));
        this.position = 1;
    }

    void tryParse() throws FMTException {
        while (scanner.hasNext()) {
            String delimetedString = scanner.next();
            if (delimetedString.equalsIgnoreCase("")) {
                continue;
            }
//            if (delimetedString.startsWith("?")) {
//                System.err.println("OVERRIDE");
//                Tuple<String, String> command = Tuple.split(delimetedString, "=");
//                switch (command.left().substring(1)) {
//                    case "DELIM":
//                        this.scanner.useDelimiter(Pattern.compile(command.right() + "|\n"));
//
//                }
//                System.err.println("OVERWRITTEN");
//            } else {
            tokens.add(Token.of(delimetedString, position));
//            }
            position += delimetedString.length();
        }
    }

    @Override
    public Iterator<Token> iterator() {
        return tokens.iterator();
    }
    Deque<Number> stack = new ArrayDeque<>();

    void processTokens() throws FMTException {
        Token current;
        while ((current = tokens.poll()) != null) {
            switch (current.type) {
                case LITERAL:
                    stack.push((Number) current.contents);
                    break;
                case OPERATOR:
                    try {
                        operate(current);
                    } catch (NoSuchElementException e) {
                        throw new FMTException(current.pos, "operator " + current.toString() + " unable to operate: not enough elements on the stack");
                    }
                    break;
                default:
            }
        }
    }

    <T extends Number> Double calc(Tuple<T, T> operands, BinaryOperator<T> op) {
        return operands.map(op).doubleValue();
    }

    private void operate(Token tok) throws NoSuchElementException, FMTException {
        String op = ((Token.OperatorToken) tok).contents;
        Tuple<Number, Number> operands;
        switch (op) {
            case "*":
            case "/":
            case "+":
            case "-":
            case "\"":
            case "^":
            case "**":
            case "l":
                operands = Tuple.of(stack.pop(), stack.pop());
                break;
            case "#":
            case "~":
                operands = Tuple.of(stack.pop(), 0);
                break;
            default:
                throw new FMTException(tok.pos, op + " unrecognised operator");
        }
        BinaryOperator<Double> bi = (l, r) -> 0.0;
        switch (op) {
            case "\"":
                operands.consume(stack::push);
                return;
            case "l":
                bi = (l, r) -> Math.log(r) / Math.log(l);
                break;
            case "^":
            case "**":
                bi = (l, r) -> Math.pow(r, l);
                break;
            case "#":
                bi = (l, r) -> Math.sqrt(l);
                break;
            case "*":
                bi = (l, r) -> l * r;
                break;
            case "/":
                bi = (l, r) -> r / l;
                break;
            case "+":
                bi = (l, r) -> l + r;
                break;
            case "-":
            case "~":
                bi = (l, r) -> r - l;
                break;
            default:

        }

        stack.push(calc(operands.map(Number::doubleValue), bi));
    }

    private Collection<Number> multiPop(int j) throws EmptyStackException {
        List<Number> ret = new ArrayList<>();
        for (int i = 0; i < j; i++) {
            ret.add(stack.pop());
        }
        return ret;
    }

    private void push(List<Number> vals) {
        vals.stream().forEach(stack::push);
    }

    static class FMTException extends Exception {

        public FMTException(int position, String reason) {
            super("@ Position " + position + ", " + reason);
        }

    }

}
