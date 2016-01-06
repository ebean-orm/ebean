package com.avaje.ebean.dbmigration;

import javax.persistence.PersistenceException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses string content into separate SQL/DDL statements.
 */
public class DdlParser {

  /**
   * Break up the sql in reader into a list of statements using the semi-colon and $$ delimiters;
   */
  public List<String> parse(StringReader reader) {

    try {
      BufferedReader br = new BufferedReader(reader);
      StatementsSeparator statements = new StatementsSeparator();

      String s;
      while ((s = br.readLine()) != null) {
        s = s.trim();
        statements.nextLine(s);
      }

      return statements.statements;

    } catch (IOException e) {
      throw new PersistenceException(e);
    }
  }


  /**
   * Local utility used to detect the end of statements / separate statements.
   * This is often just the semicolon character but for trigger/procedures this
   * detects the $$ demarcation used in the history DDL generation for MySql and
   * Postgres.
   */
  static class StatementsSeparator {

    ArrayList<String> statements = new ArrayList<String>();

    boolean trimDelimiter;

    boolean inDbProcedure;

    StringBuilder sb = new StringBuilder();

    void lineContainsDollars(String line) {
      if (inDbProcedure) {
        if (trimDelimiter) {
          line = line.replace("$$","");
        }
        endOfStatement(line);
      } else {
        // MySql style delimiter needs to be trimmed/removed
        trimDelimiter = line.equals("delimiter $$");
        if (!trimDelimiter) {
          sb.append(line).append(" ");
        }
      }
      inDbProcedure = !inDbProcedure;
    }

    void endOfStatement(String line) {
      // end of Db procedure
      sb.append(line);
      statements.add(sb.toString().trim());
      sb = new StringBuilder();
    }

    void nextLine(String line) {

      if (line.contains("$$")) {
        lineContainsDollars(line);
        return;
      }

      if (inDbProcedure) {
        sb.append(line).append(" ");
        return;
      }

      int semiPos = line.indexOf(';');
      if (semiPos == -1) {
        sb.append(line).append(" ");

      } else if (semiPos == line.length() - 1) {
        // semicolon at end of line
        endOfStatement(line);

      } else {
        // semicolon in middle of line
        String preSemi = line.substring(0, semiPos);
        endOfStatement(preSemi);
        sb.append(line.substring(semiPos + 1));
      }
    }
  }
}
