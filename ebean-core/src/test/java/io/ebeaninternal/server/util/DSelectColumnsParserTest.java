package io.ebeaninternal.server.util;

import org.junit.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class DSelectColumnsParserTest {

  @Test
  public void parse() {

    Set<String> cols = DSelectColumnsParser.parse("a,MD5(id::text) as b,c");
    assertThat(cols).containsExactly("a", "MD5(id::text) as b", "c");
  }

  @Test
  public void whitespace_is_trimmed() {

    Set<String> cols = DSelectColumnsParser.parse("a , MD5(id::text) as b  , c ");
    assertThat(cols).containsExactly("a", "MD5(id::text) as b", "c");
  }

  @Test
  public void nestedFunctions() {

    Set<String> cols = DSelectColumnsParser.parse("a , concat(id,'sd',inner(foo)) as b  , c ");
    assertThat(cols).containsExactly("a", "concat(id,'sd',inner(foo)) as b", "c");
  }

  @Test
  public void basic() {

    Set<String> cols = DSelectColumnsParser.parse("name , status  , billingAddress ");
    assertThat(cols).containsExactly("name", "status", "billingAddress");
  }

  @Test
  public void basic_noWhitespace() {

    Set<String> cols = DSelectColumnsParser.parse("a,b,c");
    assertThat(cols).containsExactly("a", "b", "c");
  }

  @Test
  public void formula_noWhitespace() {

    Set<String> cols = DSelectColumnsParser.parse("a,concat(x,y),c");
    assertThat(cols).containsExactly("a", "concat(x,y)", "c");
  }

  @Test
  public void with_logicalCast_andAsAlias() {

    Set<String> cols = DSelectColumnsParser.parse("name , concat(status,'-end')::String as fullName  , billingAddress ");
    assertThat(cols).containsExactly("name", "concat(status,'-end')::String as fullName", "billingAddress");
  }

}
