package com.avaje.ebeaninternal.server.grammer;

import com.avaje.ebeaninternal.api.SpiQuery;
import com.avaje.ebeaninternal.server.grammer.antlr.EQLLexer;
import com.avaje.ebeaninternal.server.grammer.antlr.EQLParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

/**
 * Parse EQL query language applying it to an ORM query object.
 */
public class EqlParser {

  /**
   * Parse the raw EQL query and apply it to the supplied query.
   */
  public static <T> void parse(String raw, SpiQuery<T> query) {

    EQLLexer lexer = new EQLLexer(new ANTLRInputStream(raw));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    EQLParser parser = new EQLParser(tokens);
    EQLParser.Select_statementContext context = parser.select_statement();

    EqlAdapter<T> adapter = new EqlAdapter<T>(query);

    ParseTreeWalker walker = new ParseTreeWalker();
    walker.walk(adapter, context);

    query.simplifyExpressions();
  }
}
