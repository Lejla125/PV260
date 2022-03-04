import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.FileContents;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * Helper class implementing logic for method length calculation.
 * Code below was taken from Checkstyle source code.
 * https://checkstyle.org/apidocs/src-html/com/puppycrawl/tools/checkstyle/checks/sizes/MethodLengthCheck.html
 */

public class MethodLengthHelper {
    private int length;

    public int getLength() {
        return length;
    }

    public boolean isMethodTooLong(DetailAST ast, FileContents contents, boolean countEmpty, int max) {
        final DetailAST openingBrace = ast.findFirstToken(TokenTypes.SLIST);
        if (openingBrace != null) {
            final DetailAST closingBrace = openingBrace.findFirstToken(TokenTypes.RCURLY);
            length = getLengthOfBlock(openingBrace, closingBrace, contents, countEmpty);
            if (length > max) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns length of code only without comments and blank lines.
     *
     * @param openingBrace block opening brace
     * @param closingBrace block closing brace
     * @return number of lines with code for current block
     */

    private int getLengthOfBlock(DetailAST openingBrace, DetailAST closingBrace, FileContents contents, boolean countEmpty) {
        int length = closingBrace.getLineNo() - openingBrace.getLineNo() + 1;

        if(!countEmpty){
            final int lastLine = closingBrace.getLineNo();
            // lastLine - 1 is actual last line index. Last line is line with curly brace,
            // which is always not empty. So, we make it lastLine - 2 to cover last line that
            // actually may be empty.
            for (int i = openingBrace.getLineNo() - 1; i <= lastLine - 2; i++) {
                if (contents.lineIsBlank(i) || contents.lineIsComment(i)) {
                    length--;
                }
            }
        }
        return length;
    }
}