package com.utils;

public class ExpressionEvaluator {

    private String expression;
    private int pos = -1;
    private int ch;

    public ExpressionEvaluator() {
    }

    public double evaluate(String expression) {
        // initialize the expression
        this.expression = expression;
        pos = -1;
        // parse the expression
        nextChar();
        double x = parseExpression();
        if (pos < expression.length()) {
            throw new RuntimeException("Unexpected Input: '" + (char)ch + "'");
        }
        return x;
    }

    private void nextChar() {
        if (++pos < expression.length()) {
            ch = expression.charAt(pos);
        } 
        else {
            ch = -1;
        }
    }

    private boolean eat(int charToEat) {
        while (ch == ' ') {
            nextChar();
        }
        if (ch == charToEat) {
            nextChar();
            return true;
        }
        return false;
    }

    private double parseExpression() {
        double x = parseTerm();
        while (true) {
            if (eat('+')) {
                x += parseTerm(); // addition
            } 
            else if (eat('-')) {
                x -= parseTerm(); // subtraction
            } 
            else {
                return x;
            }
        }
    }

    private double parseTerm() {
        double x = parseFactor();
        while (true) {
            if (eat('*')) {
                x *= parseFactor(); // multiplication
            } else if (eat('/')) {
                x /= parseFactor(); // division
            } else {
                return x;
            }
        }
    }
    
    private double parseFactor() {
        if (eat('+')) {
            return +parseFactor();
        }
        if (eat('-')) {
            return -parseFactor();
        }
        double x;
        int startPos = this.pos;
        if (eat('(')) {
            x = parseExpression();
            if (!eat(')')) {
                throw new RuntimeException("Missing ')'");
            }
        } 
        else if ((ch >= '0' && ch <= '9')) {
            while ((ch >= '0' && ch <= '9')) {
                nextChar();
            }
            x = Double.parseDouble(expression.substring(startPos, this.pos));
        }
        else {
            throw new RuntimeException("Unexpected Input: '" + (char)ch + "'");
        }
        return x;
    }
}

// reference: https://stackoverflow.com/questions/3422673/how-to-evaluate-a-math-expression-given-in-string-form