/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package reversepolishparser;

import com.akinevz.utils.Throwing;

/**
 *
 * @author zugbug
 */
abstract class Token<T> {

    static Token of(String content, int pos) throws Tokeniser.FMTException {
        try {
            if (Throwing.passes(() -> new Double(content))) {
                return new NumberToken(Double.parseDouble(content), pos);
            }
            if (content.matches("[\"#()=;{}\\[\\]+\\-*/&!%^|<>']*")) {
                return new OperatorToken(content, pos);
            }
        } catch (Exception e) {
            throw new Tokeniser.FMTException(pos, "Unexpected: " + e.getMessage());
        }
        throw new Tokeniser.FMTException(pos, "Error parsing \"" + content + "\": not a valid literal or operator");
    }
    final int pos;

    enum TokenType {
        LITERAL, OPERATOR
    }

    static class NumberToken extends Token<Number> {

        public NumberToken(Number content, int pos) {
            super(content, TokenType.LITERAL, pos);
        }
    }

    static class OperatorToken extends Token<String> {

        public OperatorToken(String content, int pos) {
            super(content, TokenType.OPERATOR, pos);
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
        return "[" + contents + "]";
    }

}
