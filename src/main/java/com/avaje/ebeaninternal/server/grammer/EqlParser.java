package com.avaje.ebeaninternal.server.grammer;

import com.avaje.ebean.Query;
import com.avaje.ebeaninternal.server.grammer.antlr.EQLLexer;
import com.avaje.ebeaninternal.server.grammer.antlr.EQLParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class EqlParser {

  public static <T> void parse(String raw, Query<T> query) {

    EQLLexer lexer = new EQLLexer(new ANTLRInputStream(raw));
    CommonTokenStream tokens = new CommonTokenStream(lexer);
    EQLParser parser = new EQLParser(tokens);
    EQLParser.Select_statementContext context = parser.select_statement();

    EqlAdapter<T> adapter = new EqlAdapter<T>(query);

    ParseTreeWalker walker = new ParseTreeWalker();
    walker.walk(adapter, context);
  }
}
