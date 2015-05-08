package com.avaje.ebeaninternal.server.lib.util;

/**
 * Builds a string from a stack trace.
 * <p>
 * Generally used to flatten a stack trace into a single string
 * removing \r\n and limiting the size of any given stack.
 * </p>
 */
public class ThrowablePrinter {

    private static final String atString = "        at ";

    private String newLineChar = "\\r\\n";
    
    private int maxStackTraceLines = 3;

    /**
     * Set the maximum number of lines in any one part of 
     * the stack trace. This is not the total maximum.
     */
    public void setMaxStackTraceLines(int maxStackTraceLines) {
        this.maxStackTraceLines = maxStackTraceLines;
    }

    /**
     * Set the new line character used to replace \r\n with.
     * This is useful so that the stack is placed on a single line
     * in a log file.
     */
    public void setNewLineChar(String newLineChar) {
        this.newLineChar = newLineChar;
    }

    /**
     * Convert the error into a string representation.
     * <p>
     * Replaces the \r\n and limits the stack lines.
     * </p>
     */
    public String print(Throwable e) {
        StringBuffer sb = new StringBuffer();
        printThrowable(sb, e, false);
        
        String line = sb.toString();
        line = StringHelper.replaceString(line, "\r", "\\r");
        line = StringHelper.replaceString(line, "\n", "\\n");

        return line;
    }
    
    /**
     * Recursively output the Throwable stack trace to the log.
     * 
     * @param sb the buffer to write the stack trace to
     * @param e the source throwable
     * @param isCause flag to indicate if this is the top level throwable or a
     *            cause
     */
    protected void printThrowable(StringBuffer sb, Throwable e, boolean isCause) {
        if (e != null) {
            if (isCause) {
                sb.append("Caused by: ");
            }
            sb.append(e.getClass().getName());
            sb.append(":");
            sb.append(e.getMessage()).append(newLineChar);

            StackTraceElement[] ste = e.getStackTrace();
            int outputStackLines = ste.length;
            int notShownCount = 0;
            if (ste.length > maxStackTraceLines) {
                outputStackLines = maxStackTraceLines;
                notShownCount = ste.length - outputStackLines;
            }
            for (int i = 0; i < outputStackLines; i++) {
                sb.append(atString);
                sb.append(ste[i].toString()).append(newLineChar);
            }
            if (notShownCount > 0) {
                sb.append("        ... ");
                sb.append(notShownCount);
                sb.append(" more").append(newLineChar);
            }
            Throwable cause = e.getCause();
            if (cause != null) {
                printThrowable(sb, cause, true);
            }
        }
    }
}
