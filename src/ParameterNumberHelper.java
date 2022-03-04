import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;
import com.puppycrawl.tools.checkstyle.utils.AnnotationUtil;

/**
 * Helper class implementing logic for method parameter number calculation.
 * Code below was taken from Checkstyle source code.
 * https://checkstyle.org/xref/com/puppycrawl/tools/checkstyle/checks/sizes/ParameterNumberCheck.html
 */

public class ParameterNumberHelper {
    /** {@link Override Override} annotation name.*/
    private static final String OVERRIDE = "Override";

    /** Canonical {@link Override Override} annotation name.*/
    private static final String CANONICAL_OVERRIDE = "java.lang." + OVERRIDE;

    private int count;

    public int getCount() {
        return count;
    }

    public boolean areThereTooManyParameters(DetailAST ast, int max, boolean ignore) {
        final DetailAST params = ast.findFirstToken(TokenTypes.PARAMETERS);
        count = params.getChildCount(TokenTypes.PARAMETER_DEF);
        if (count > max && !shouldIgnoreNumberOfParameters(ast, ignore)) {
            return true;
        }
        return false;
    }

    /**
     * Determine whether to ignore number of parameters for the method.
     *
     * @param ast the token to process
     * @return true if this is overridden method and number of parameters should be ignored
     * false otherwise
     */
    private boolean shouldIgnoreNumberOfParameters(DetailAST ast, boolean ignoreOverriddenMethods) {
        // if you override a method, you have no power over the number of parameters
        return ignoreOverriddenMethods
                && (AnnotationUtil.containsAnnotation(ast, OVERRIDE)
                || AnnotationUtil.containsAnnotation(ast, CANONICAL_OVERRIDE));
    }
}
