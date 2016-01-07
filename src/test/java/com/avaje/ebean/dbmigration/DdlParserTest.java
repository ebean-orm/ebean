package com.avaje.ebean.dbmigration;

import org.junit.Test;

import java.io.StringReader;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class DdlParserTest {

  DdlParser parser = new DdlParser();

  @Test
  public void parse_ignoresEmptyLines() throws Exception {

    List<String> stmts = parser.parse(new StringReader("\n\none;\n\ntwo;\n\n"));

    assertThat(stmts).hasSize(2);
    assertThat(stmts).contains("one;","two;");
  }

  @Test
  public void parse_ignoresComments_whenFirst() throws Exception {

    List<String> stmts = parser.parse(new StringReader("-- comment\ntwo;"));

    assertThat(stmts).hasSize(1);
    assertThat(stmts).contains("two;");
  }

  @Test
  public void parse_ignoresEmptyLines_whenFirst() throws Exception {

    List<String> stmts = parser.parse(new StringReader("\n\n-- comment\ntwo;\n\n"));
    assertThat(stmts).hasSize(1);
    assertThat(stmts).contains("two;");
  }

  @Test
  public void parse_inlineEmptyLines_replacedWithSpace() throws Exception {

    List<String> stmts = parser.parse(new StringReader("\n\n-- comment\none\ntwo;\n\n"));
    assertThat(stmts).hasSize(1);
    assertThat(stmts).contains("one two;");
  }


  @Test
  public void parse_ignoresComments() throws Exception {

    List<String> stmts = parser.parse(new StringReader("one;\n-- comment\ntwo;"));

    assertThat(stmts).hasSize(2);
    assertThat(stmts).contains("one;","two;");
  }
}