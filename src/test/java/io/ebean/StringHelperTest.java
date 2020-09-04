package io.ebean;

import io.ebean.util.StringHelper;
import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class StringHelperTest {

  @Test
  public void testSplitNames() {
    assertThat(StringHelper.splitNames("")).hasSize(0);
    assertThat(StringHelper.splitNames(" , ;")).hasSize(0);
    assertThat(StringHelper.splitNames("foo bar")).containsExactly("foo", "bar");
    assertThat(StringHelper.splitNames(" foo \n bar    ")).containsExactly("foo", "bar");
    assertThat(StringHelper.splitNames(" foo , bar ;")).containsExactly("foo", "bar");
    assertThat(StringHelper.splitNames("foo, bar")).containsExactly("foo", "bar");
    assertThat(StringHelper.splitNames("foo, bar baz")).containsExactly("foo", "bar", "baz");
    assertThat(StringHelper.splitNames("foo, bar\nbaz")).containsExactly("foo", "bar", "baz");
  }

  @Test
  public void testReplaceStringMulti() {
    String content = "This is\na\rmultiline\r\ntext\n\r";
    String[] multi = {"\r\n", "\r", "\n"};
    content = StringHelper.replaceStringMulti(content, multi, "<br/>");
    assertThat(content).isEqualTo("This is<br/>a<br/>multiline<br/>text<br/><br/>");
  }

  @Test
  public void testDelimitedToMap() {
    String content = "name1=blah; name2 = blubb ;name3\n=foo";
    Map<String, String> map = StringHelper.delimitedToMap(content, ";", "=");
    assertThat(map)
      .containsEntry("name1", "blah")
      .containsEntry("name2", " blubb ")  // white space is not trimmed
      .containsEntry("name3", "foo");
  }

}
