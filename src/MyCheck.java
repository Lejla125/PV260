import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.CheckUtil;

import static java.lang.Integer.sum;


/** The code for the MyCheck class was inspired by Checkstyle source code. */
public class MyCheck extends AbstractCheck {

    //Parameters for LOC of a method.
    /** Default maximum number of lines.*/
    private static final int DEFAULT_MAX_LINES = 150;
    /** Specify the maximum number of lines allowed.*/
    private int maxLength = DEFAULT_MAX_LINES;
    /** Specify whether to count empty lines.*/
    private boolean countEmpty;
    private boolean methodTooLong;

    private MethodLengthHelper methodLengthHelper;

    //Parameters for cyclomatic complexity of a method
    /** Default allowed complexity.*/
    private static final int DEFAULT_COMPLEXITY_VALUE = 10;
    /** Specify the maximum cyclomatic complexity allowed.*/
    private int maxComplexity = DEFAULT_COMPLEXITY_VALUE;
    /** Specify whether to treat the whole switch block as a single decision point.*/
    private boolean switchBlockAsSingleDecisionPoint;
    private boolean methodTooComplex;

    private CyclomaticComplexityHelper cyclomaticComplexityHelper;

    //Parameters for nesting depth of method control logic.
    /** Specify maximum allowed nesting depths. */
    private int maxIfDepth = 1;
    private int maxForDepth = 1;
    private int maxTryDepth = 1;
    /** Current nesting depths.*/
    private int ifDepth;
    private int forDepth;
    private int tryDepth;
    private boolean deepNestingLevel;

    //Parameters for number of variables (local variables + method parameters).
    /** Default maximum number of variables.*/
    private static final int DEFAULT_MAX_VARIABLES = 10;
    /** Specify maximum allowed variable count. */
    private int maxVariableCount = DEFAULT_MAX_VARIABLES;
    /** Current variable count. */
    private int variableCount;
    /** Ignore number of parameters for methods with {@code @Override} annotation.*/
    private boolean ignoreOverriddenMethods;
    private boolean tooManyVariables;

    private ParameterNumberHelper parameterNumberHelper;

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public void setCountEmpty(boolean countEmpty) {
        this.countEmpty = countEmpty;
    }

    public void setMaxComplexity(int maxComplexity) {
        this.maxComplexity = maxComplexity;
    }

    public void setSwitchBlockAsSingleDecisionPoint(boolean switchBlockAsSingleDecisionPoint) {
        this.switchBlockAsSingleDecisionPoint = switchBlockAsSingleDecisionPoint;
    }

    public void setMaxIfDepth(int maxIfDepth) {
        this.maxIfDepth = maxIfDepth;
    }

    public void setMaxForDepth(int maxForDepth) {
        this.maxForDepth = maxForDepth;
    }

    public void setMaxTryDepth(int maxTryDepth) {
        this.maxTryDepth = maxTryDepth;
    }

    public void setIgnoreOverriddenMethods(boolean ignoreOverriddenMethods) {
        this.ignoreOverriddenMethods = ignoreOverriddenMethods;
    }

    public void setMaxVariableCount(int maxVariableCount) { this.maxVariableCount = maxVariableCount; }

    public MyCheck() {
        countEmpty = true;
        methodTooLong = false;
        methodTooComplex = false;
        deepNestingLevel = false;
        tooManyVariables = false;
        variableCount = 0;
        methodLengthHelper = new MethodLengthHelper();
        cyclomaticComplexityHelper = new CyclomaticComplexityHelper();
        parameterNumberHelper = new ParameterNumberHelper();
    }

    @Override
    public int[] getDefaultTokens() {
        return new int[]{
                TokenTypes.METHOD_DEF,
                TokenTypes.CTOR_DEF,
                TokenTypes.COMPACT_CTOR_DEF,
                TokenTypes.INSTANCE_INIT,
                TokenTypes.STATIC_INIT,
                TokenTypes.LITERAL_WHILE,
                TokenTypes.LITERAL_DO,
                TokenTypes.LITERAL_FOR,
                TokenTypes.LITERAL_IF,
                TokenTypes.LITERAL_SWITCH,
                TokenTypes.LITERAL_CASE,
                TokenTypes.LITERAL_CATCH,
                TokenTypes.LITERAL_TRY,
                TokenTypes.QUESTION,
                TokenTypes.LAND,
                TokenTypes.LOR,
                TokenTypes.VARIABLE_DEF
        };
    }

    @Override
    public int[] getAcceptableTokens() {
        return getDefaultTokens();
    }

    @Override
    public int[] getRequiredTokens() {
        return getDefaultTokens();
    }

    @Override
    public void beginTree(DetailAST rootAST) {
        ifDepth = 0;
        forDepth = 0;
        tryDepth = 0;
    }

    // suppress deprecation until https://github.com/checkstyle/checkstyle/issues/11166
    @Override
    @SuppressWarnings("deprecation")
    public void visitToken(DetailAST ast) {
        switch(ast.getType()) {
        case TokenTypes.METHOD_DEF:
        case TokenTypes.CTOR_DEF:
        case TokenTypes.COMPACT_CTOR_DEF:
            if(ast.getType() == TokenTypes.METHOD_DEF || ast.getType() == TokenTypes.CTOR_DEF) {
                tooManyVariables = parameterNumberHelper.areThereTooManyParameters(
                        ast, maxVariableCount, ignoreOverriddenMethods
                );
            }
            final FileContents contents = getFileContents();
            methodTooLong = methodLengthHelper.isMethodTooLong(ast, contents, countEmpty, maxLength);
            cyclomaticComplexityHelper.visitMethodDef();
            break;
        case TokenTypes.INSTANCE_INIT:
        case TokenTypes.STATIC_INIT:
            cyclomaticComplexityHelper.visitMethodDef();
            break;
        case TokenTypes.VARIABLE_DEF:
            ++variableCount;
            break;
        case TokenTypes.LITERAL_FOR:
            if(forDepth > maxForDepth) {
                deepNestingLevel = true;
            }
            ++forDepth;
            break;
        case TokenTypes.LITERAL_TRY:
            if(tryDepth > maxTryDepth) {
                deepNestingLevel = true;
            }
            ++tryDepth;
            break;
        default:
            cyclomaticComplexityHelper.visitTokenHook(ast, switchBlockAsSingleDecisionPoint);
        }

        if (!CheckUtil.isElseIf(ast)) {
            if (ifDepth > maxIfDepth) {
                deepNestingLevel = true;
            }
            ++ifDepth;
        }
    }

    @Override
    public void leaveToken(DetailAST ast) {
        switch(ast.getType()) {
        case TokenTypes.METHOD_DEF:
        case TokenTypes.CTOR_DEF:
        case TokenTypes.COMPACT_CTOR_DEF:
        case TokenTypes.INSTANCE_INIT:
        case TokenTypes.STATIC_INIT:
            methodTooComplex = cyclomaticComplexityHelper.leaveMethodDef(maxComplexity);
            break;
        case TokenTypes.VARIABLE_DEF:
            if(variableCount > maxVariableCount) tooManyVariables = true;
            break;
        case TokenTypes.LITERAL_FOR:
            --forDepth;
            break;
        case TokenTypes.LITERAL_TRY:
            --tryDepth;
            break;
        default:
            break;
        }

        if (!CheckUtil.isElseIf(ast)) {
            --ifDepth;
        }
        logBrainMethod(ast);
    }

    private void logBrainMethod(DetailAST ast) {
        if(methodTooLong && methodTooComplex && deepNestingLevel && tooManyVariables) {
            final String methodName = ast.findFirstToken(TokenTypes.IDENT).getText();
            log(ast, "Method: " + methodName + " is a Brain method.\r\n"
                    + "LOC: " + methodLengthHelper.getLength() + "\r\n"
                    + "Cyclomatic complexity: " + cyclomaticComplexityHelper.getCriticalComplexity() + "\r\n"
                    + "Nesting level over maximum allowed.\r\n"
                    + "Variable count: " + sum(variableCount, parameterNumberHelper.getCount()));
        }
    }
}
