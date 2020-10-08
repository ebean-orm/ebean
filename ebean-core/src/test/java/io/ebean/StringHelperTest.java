package io.ebean;

import io.ebean.util.StringHelper;
import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class StringHelperTest {

  @Test
  public void isNull() {
    assertTrue(StringHelper.isNull(null));
    assertTrue(StringHelper.isNull(""));
    assertTrue(StringHelper.isNull("   "));
    assertFalse(StringHelper.isNull("a"));
  }

  @Test
  public void replaceString() {
    assertEquals("sJmethJng", StringHelper.replace("somethong", "o","J"));
    assertEquals("somethong", StringHelper.replace("somethong", "o", null));
    assertNull(StringHelper.replace(null, "o","J"));
  }

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
  public void removeNewLines() {
    String content = "This is\na\rmultiline\r\ntext\n\r";
    content = StringHelper.removeNewLines(content);
    assertThat(content).isEqualTo("This is a multiline  text  ");
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

  @Test
  public void testDelimitedToMap_expect_trimLeading() {
    String content = ";name1=foo;name2=bar;";
    Map<String, String> map = StringHelper.delimitedToMap(content, ";", "=");
    assertThat(map).hasSize(2)
      .containsEntry("name1", "foo")
      .containsEntry("name2", "bar");
  }

  @Test
  public void testDelimitedToMap_when_emptyEntry() {
    String content = ";name1=foo;=;name2=bar;";
    Map<String, String> map = StringHelper.delimitedToMap(content, ";", "=");
    assertThat(map).hasSize(2)
      .containsEntry("name1", "foo")
      .containsEntry("name2", "bar");
  }

  @Test
  public void testDelimitedToMap_when_missingValue() {
    String content = ";name1=foo;nameX;name2=bar;";
    Map<String, String> map = StringHelper.delimitedToMap(content, ";", "=");
    assertThat(map).hasSize(3)
      .containsEntry("nameX", null)
      .containsEntry("name1", "foo")
      .containsEntry("name2", "bar");
  }

  @Test
  public void testDelimitedToMap_when_missingValueAtEnd() {
    String content = ";name1=foo;nameX;name2=bar;nameX2";
    Map<String, String> map = StringHelper.delimitedToMap(content, ";", "=");
    assertThat(map).hasSize(3)
      .containsEntry("nameX", null)
      .containsEntry("name1", "foo")
      .containsEntry("name2", "bar");
  }

  @Test
  public void testDelimitedToMap_when_null() {
    Map<String, String> map = StringHelper.delimitedToMap(null, ";", "=");
    assertThat(map).isEmpty();
  }

  @Test
  public void testDelimitedToMap_when_empty() {
    Map<String, String> map = StringHelper.delimitedToMap("", ";", "=");
    assertThat(map).isEmpty();
  }
}
