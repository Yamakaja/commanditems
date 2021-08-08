package me.yamakaja.commanditems.data.action;

import com.fasterxml.jackson.annotation.JsonProperty;
import me.yamakaja.commanditems.CommandItems;
import me.yamakaja.commanditems.data.ItemDefinition;
import me.yamakaja.commanditems.interpreter.InterpretationContext;

import java.util.*;

public class ActionMathExpr extends Action {

    @JsonProperty(required = true)
    private String target;

    @JsonProperty(required = true)
    private String expr;

    @JsonProperty(defaultValue = "false")
    private boolean round;

    @JsonProperty(required = true)
    private Action[] actions;

    private transient Expression ast;

    public ActionMathExpr() {
        super(ActionType.MATH_EXPR);
    }

    @Override
    public void trace(List<ItemDefinition.ExecutionTrace> trace, int depth) {
        String line = String.format("%s = %s%s", target, this.round ? "(rounded) " : "", expr);

        trace.add(new ItemDefinition.ExecutionTrace(depth, line));
        for (Action action : this.actions) action.trace(trace, depth + 1);
    }

    @Override
    public void init() {
        try {
            this.ast = parse(this.expr);

            for (Action action : this.actions) action.init();
        } catch (RuntimeException e) {
            throw new RuntimeException("Failed to parse math expression: ", e);
        }
    }

    @FunctionalInterface
    public
    interface Expression {
        double eval(Map<String, Double> params);
    }

    // ====================================================
    // Derived from: https://stackoverflow.com/a/26227947
    // ====================================================
    public static Expression parse(final String str) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            Expression parse() {
                nextChar();
                Expression x = parseExpression();
                if (pos < str.length()) throw new RuntimeException("Unexpected: " + (char) ch);
                return x;
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            //        | number | functionName factor | factor `^` factor

            Expression parseExpression() {
                Expression x = parseTerm();
                for (; ; ) {
                    if (eat('+')) {
                        Expression a = x;
                        Expression b = parseTerm();
                        x = (params) -> (a.eval(params) + b.eval(params)); // addition
                    } else if (eat('-')) {
                        Expression a = x;
                        Expression b = parseTerm();
                        x = (params) -> (a.eval(params) - b.eval(params)); // subtraction
                    } else return x;
                }
            }

            Expression parseTerm() {
                Expression x = parseFactor();
                for (; ; ) {
                    if (eat('*')) {
                        Expression a = x;
                        Expression b = parseFactor();
                        x = (params) -> (a.eval(params) * b.eval(params));
                    } else if (eat('/')) {
                        Expression a = x;
                        Expression b = parseFactor();
                        x = (params) -> (a.eval(params) / b.eval(params));
                    } else return x;
                }
            }

            Expression parseFactor() {
                if (eat('+')) return parseFactor(); // unary plus
                if (eat('-')) {
                    Expression x = parseFactor();
                    return (params) -> -x.eval(params); // unary minus
                }

                Expression x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    double res = Double.parseDouble(str.substring(startPos, this.pos));
                    x = (params) -> res;
                } else if (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z') { // symbols. May not start with a number or underscore
                    while (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch >= '0' && ch <= '9' || ch == '_')
                        nextChar();
                    String symbolName = str.substring(startPos, this.pos);
                    if (eat('(')) {
                        switch (symbolName) {
                            case "sqrt": {
                                Expression a = parseExpression();
                                x = (params) -> Math.sqrt(a.eval(params));
                                break;
                            }
                            case "sin": {
                                Expression a = parseExpression();
                                x = (params) -> Math.sin(a.eval(params));
                                break;
                            }
                            case "asin": {
                                Expression a = parseExpression();
                                x = (params) -> Math.asin(a.eval(params));
                                break;
                            }
                            case "cos": {
                                Expression a = parseExpression();
                                x = (params) -> Math.cos(a.eval(params));
                                break;
                            }
                            case "acos": {
                                Expression a = parseExpression();
                                x = (params) -> Math.acos(a.eval(params));
                                break;
                            }
                            case "tan": {
                                Expression a = parseExpression();
                                x = (params) -> Math.tan(a.eval(params));
                                break;
                            }
                            case "atan": {
                                Expression a = parseExpression();
                                x = (params) -> Math.atan(a.eval(params));
                                break;
                            }
                            case "ceil": {
                                Expression a = parseExpression();
                                x = (params) -> Math.ceil(a.eval(params));
                                break;
                            }
                            case "floor": {
                                Expression a = parseExpression();
                                x = (params) -> Math.floor(a.eval(params));
                                break;
                            }
                            case "abs": {
                                Expression a = parseExpression();
                                x = (params) -> Math.abs(a.eval(params));
                                break;
                            }
                            case "exp": {
                                Expression a = parseExpression();
                                x = (params) -> Math.exp(a.eval(params));
                                break;
                            }
                            case "log": {
                                Expression a = parseExpression();
                                x = (params) -> Math.log(a.eval(params));
                                break;
                            }
                            case "round": {
                                Expression a = parseExpression();
                                x = (params) -> Math.round(a.eval(params));
                                break;
                            }
                            case "min": {
                                List<Expression> expressionList = new ArrayList<>();
                                do {
                                    Expression a = parseExpression();
                                    expressionList.add(a);
                                } while (eat(','));
                                x = (params) -> {
                                    double min = expressionList.get(0).eval(params);
                                    for (int i = 1; i < expressionList.size(); i++) {
                                        double v = expressionList.get(i).eval(params);
                                        if (v < min)
                                            min = v;
                                    }
                                    return min;
                                };
                                break;
                            }
                            case "max": {
                                List<Expression> expressionList = new ArrayList<>();
                                do {
                                    Expression a = parseExpression();
                                    expressionList.add(a);
                                } while (eat(','));
                                x = (params) -> {
                                    double max = expressionList.get(0).eval(params);
                                    for (int i = 1; i < expressionList.size(); i++) {
                                        double v = expressionList.get(i).eval(params);
                                        if (v > max)
                                            max = v;
                                    }
                                    return max;
                                };
                                break;
                            }
                            case "fmod": {
                                Expression a = parseExpression();
                                if (!eat(','))
                                    throw new RuntimeException("fmod requires two parameters!");
                                Expression b = parseExpression();

                                x = (params) -> a.eval(params) % b.eval(params);
                                break;
                            }
                            case "sign": {
                                Expression a = parseExpression();

                                x = (params) -> Math.signum(a.eval(params));
                                break;
                            }
                            case "rand":
                                x = (params) -> Math.random();
                                break;

                            case "randn": {
                                Random random = new Random();
                                x = (params) -> random.nextGaussian();
                                break;
                            }
                            default:
                                throw new RuntimeException("Unknown function: " + symbolName);
                        }
                        if (!eat(')'))
                            throw new RuntimeException("Failed to find closing ')'.");
                    } else {
                        // Variable
                        if ("pi".equals(symbolName))
                            x = (params) -> Math.PI;

                        else if ("e".equals(symbolName))
                            x = (params) -> Math.E;

                        else x = (params) -> {
                                if (!params.containsKey(symbolName))
                                    throw new RuntimeException("Tried to access undefined variable: " + symbolName);

                                return params.get(symbolName);
                            };
                    }
                } else {
                    throw new RuntimeException("Unexpected: " + (char) ch);
                }

                if (eat('^')) {
                    Expression p = parseFactor();
                    return (params) -> Math.pow(x.eval(params), p.eval(params)); // exponentiation
                }

                if (eat('%')) {
                    Expression m = parseFactor();
                    return (params) -> x.eval(params) % m.eval(params); // fmod
                }

                return x;
            }
        }.parse();
    }
    // ====================================================

    @Override
    public void process(InterpretationContext context) {
        context.pushFrame();

        Map<String, Double> params = new HashMap<>();
        context.forEachNumericLocal(params::put);

        double rval = this.ast.eval(params);
        if (this.round)
            context.pushLocal(this.target, Long.toString(Math.round(rval)));
        else
            context.pushLocal(this.target, String.format("%f", rval));

        for (Action action : this.actions)
            action.process(context);

        context.popFrame();
    }

}
