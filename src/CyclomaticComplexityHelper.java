import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Helper class implementing logic for method cyclomatic complexity calculation.
 * Code below was taken from Checkstyle source code.
 * https://checkstyle.sourceforge.io/apidocs/src-html/com/puppycrawl/tools/checkstyle/checks/metrics/CyclomaticComplexityCheck.html
 */

public class CyclomaticComplexityHelper {
    /** The initial current value.*/
    private static final BigInteger INITIAL_VALUE = BigInteger.ONE;
    /** Stack of values - all but the current value.*/
    private final Deque<BigInteger> valueStack = new ArrayDeque<>();
    /** The current value.*/
    private BigInteger currentValue = INITIAL_VALUE;
    private BigInteger criticalComplexity;

    public BigInteger getCriticalComplexity() {
        return criticalComplexity;
    }

    /**
     * Hook called when visiting a token. Will not be called the method definition tokens.
     *
     * @param ast the token being visited
     * @param switchBlockAsSingleDecisionPoint whether to treat the whole switch block as one decision point
     */
    public void visitTokenHook(DetailAST ast, boolean switchBlockAsSingleDecisionPoint) {
        if (switchBlockAsSingleDecisionPoint) {
            if (ast.getType() != TokenTypes.LITERAL_CASE) {
                incrementCurrentValue(BigInteger.ONE);
            }
        } else if (ast.getType() != TokenTypes.LITERAL_SWITCH) {
            incrementCurrentValue(BigInteger.ONE);
        }
    }

    /** Process the end of a method definition.*/
    public boolean leaveMethodDef(int max) {
        boolean complexity = false;
        final BigInteger bigIntegerMax = BigInteger.valueOf(max);
        if (currentValue.compareTo(bigIntegerMax) > 0) {
            complexity = true;
            criticalComplexity = currentValue;
        }
        popValue();
        return complexity;
    }

    /**
     * Increments the current value by a specified amount.
     *
     * @param amount the amount to increment by
     */
    private void incrementCurrentValue(BigInteger amount) {
        currentValue = currentValue.add(amount);
    }

    /** Push the current value on the stack.*/
    private void pushValue() {
        valueStack.push(currentValue);
        currentValue = INITIAL_VALUE;
    }

    /** Pops a value off the stack and makes it the current value.*/
    private void popValue() {
        currentValue = valueStack.pop();
    }

    /** Process the start of the method definition.*/
    public void visitMethodDef() {
        pushValue();
    }
}