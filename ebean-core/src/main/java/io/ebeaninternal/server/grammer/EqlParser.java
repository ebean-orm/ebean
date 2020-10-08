package io.ebeaninternal.server.grammer;

import io.ebean.ExpressionFactory;
import io.ebean.ExpressionList;
import io.ebeaninternal.api.SpiQuery;
import io.ebeaninternal.server.grammer.antlr.EQLLexer;
import io.ebeaninternal.server.grammer.antlr.EQLParser;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

/**
 * Parse EQL query language applying it to an ORM query object.
 */
public class EqlParser {

  private static final ErrorListener errorListener = new ErrorListener();

  /**
   * Parse the raw EQL query and apply it to the supplied query.
   */
  public static <T> void parse(String raw, SpiQuery<T> query) {

    EQLParser parser = new EQLParser(new CommonTokenStream(new EQLLexer(CharStreams.fromString(raw))));
    parser.addErrorListener(errorListener);

    new ParseTreeWalker().walk(new EqlAdapter<>(query), parser.select_statement());
    query.simplifyExpressions();
  }

  public static <T> void parseWhere(String raw, ExpressionList<T> where, ExpressionFactory expr, Object[] params) {

    EQLParser parser = new EQLParser(new CommonTokenStream(new EQLLexer(CharStreams.fromString(raw))));
    parser.addErrorListener(errorListener);

    new ParseTreeWalker().walk(new EqlWhereAdapter<>(where, expr, params), parser.conditional_expression());
  }

  static class ErrorListener extends BaseErrorListener {

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {

      String reportMsg = "line " + line + ":" + charPositionInLine + " " + msg;
      throw new IllegalArgumentException(reportMsg);
    }
  }
}
