/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reversepolishparser;

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
import java.util.Stack;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.regex.Pattern;
import reversepolishparser.Tokeniser.Token;

/**
 *
 * @author zugbug
 */
class Tokeniser implements Iterable<Token> {

    @Override
    public String toString() {
        return stack.toString();
    }

    private final Scanner thing;
    Queue<Token> tokens = new ArrayDeque<>();

    int position;

    Tokeniser(String lines) {
        this(lines, " ");
    }

    Tokeniser(String lines, String delim) {
        this.thing = new Scanner(lines)
                .useDelimiter(Pattern.compile(delim + "|\n"));
        this.position = 0;
    }

   void tryParse() throws FMTException {
        while (thing.hasNext()) {
            String delimetedString = thing.next();
            position += delimetedString.length();
            if (delimetedString.equalsIgnoreCase("")) {
                continue;
            }
            tokens.add(Token.of(delimetedString, position));
        }
    }

    @Override
    public Iterator<Token> iterator() {
        return tokens.iterator();
    }
    Deque<Number> stack = new ArrayDeque<>();

    void processTokens() throws FMTException{
        Token current;
        while ((current = tokens.poll()) != null) {
            switch (current.type) {
                case LITERAL:
                    stack.push((Number) current.contents);
                    break;
                case OPERATOR:
                    try {
                        operate((Character) current.contents);
                    } catch (NoSuchElementException e) {
                        throw new FMTException(current.pos, "operator");
                    }
                    break;
                default:
            }
        }
    }

    <T extends Number> Double calc(Pair<T> operands, BinaryOperator<T> op) {
        return op.apply(operands.left, operands.right).doubleValue();
    }

    private void operate(Character op) throws NoSuchElementException {

        Pair<Number> operands;
        switch (op) {
            case '*':
            case '/':
            case '+':
            case '-':
            case '"':
                operands = new Pair<>(stack.pop(), stack.pop());
                break;
            case '~':
                operands = new Pair<>(stack.pop(), 0);
                break;
            default:
                return;
        }
        BinaryOperator<Double> bi = (l, r) -> 0.0;
        switch (op) {
            case '"':
                stack.push(operands.left);
                stack.push(operands.right);
                return;
            case '*':
                bi = (l, r) -> l * r;
                break;
            case '/':
                bi = (l, r) -> r / l;
                break;
            case '+':
                bi = (l, r) -> l + r;
                break;
            case '-':
            case '~':
                bi = (l, r) -> r - l;
                break;
            default:

        }

        stack.push(calc(operands.map(s -> s.doubleValue()), bi));
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

    public class Pair<T> {

        <U> Pair<U> map(Function<T, U> mapper) {
            return new Pair<>(mapper.apply(left), mapper.apply(right));
        }

        T left, right;

        public Pair(T left, T right) {
            this.left = left;
            this.right = right;
        }

        public Pair(Collection<? extends T> src) {
            Iterator<?> t = src.iterator();
            this.left = (T) t.next();
            this.right = (T) t.next();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj.getClass() != Pair.class) {
                return false;
            }
            Pair t = (Pair) obj;
            return ((this.left == t.left) && (this.right == t.right))
                    || ((this.left == t.right) && (this.right == t.left));
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 29 * hash + this.left.hashCode() + this.right.hashCode();
            return hash;
        }

        private Pair<T> swap() {
            return new Pair<>(right, left);
        }

    }

    public static abstract class Token<T> {

        static Token of(String content, int pos) throws FMTException {
            try {
                if (Character.isLetterOrDigit(content.charAt(0))) {
                    return new NumberToken(Double.parseDouble(content), pos);
                }
                if (content.length() == 1) {
                    return new OperatorToken(content.charAt(0), pos);
                }
            } catch (Exception e) {
                throw new FMTException(pos, "\nError parsing: +\"" + e.getMessage()+"\"");
            }
            throw new FMTException(pos, "\nError parsing: \"" + content+"\"");
        }
        private final int pos;

        enum TokenType {

            LITERAL, OPERATOR;
        }

        static class NumberToken extends Token<Number> {

            public NumberToken(Number content, int pos) {
                super(content, TokenType.LITERAL,pos);
            }

        }

        static class OperatorToken extends Token<Character> {

            public OperatorToken(Character content,int pos) {
                super(content, TokenType.OPERATOR,pos);
            }

        }

        private Token(T content, TokenType type, int pos) {
            this.pos = pos;
            this.contents = content;
            this.type = type;
        }

        final T contents;
        final TokenType type;

        @Override
        public String toString() {
            return "[" + contents + "]~" + type;
        }

    }

    static class FMTException extends Exception {

        public FMTException(int position, String token) {
            super("Error at position " + position + ", " + token + " is not valid here");
        }

    }

}
