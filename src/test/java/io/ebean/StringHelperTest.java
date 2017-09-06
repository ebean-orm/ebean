package io.ebean;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.Test;

import io.ebean.util.StringHelper;

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
    String[] multi = { "\r\n", "\r", "\n" };
    content = StringHelper.replaceStringMulti(content, multi, "<br/>");
    assertThat(content).isEqualTo("This is<br/>a<br/>multiline<br/>text<br/><br/>");
  }
  
  @Test
  public void testParseNameQuotedValue() {
    String content = "name1='value1' name2='value2'";
    Map<String, String> map = StringHelper.parseNameQuotedValue(content);
    assertThat(map).containsEntry("name1", "value1").containsEntry("name2", "value2");
    
    // now check with double quotes
    content = "name1=\"value1\" name2=\"value2\"";
    map = StringHelper.parseNameQuotedValue(content);
    assertThat(map).containsEntry("name1", "value1").containsEntry("name2", "value2");
    
    // now check mix
    content = "name1=\"value'1\" name2='value\"2'";
    map = StringHelper.parseNameQuotedValue(content);
    assertThat(map).containsEntry("name1", "value'1").containsEntry("name2", "value\"2");
  }
  
  @Test
  public void testCountOccurances() {
    String content = "blablabla, bla bla blubb, blubblubb blablub blubblabla ";
    assertThat(StringHelper.countOccurances(content , "bla")).isEqualTo(8);
    assertThat(StringHelper.countOccurances(content , "blubb")).isEqualTo(3);
    assertThat(StringHelper.countOccurances(content , "blah")).isEqualTo(0);
  }
  
  @Test
  public void testDelimitedToMap() {
    String content = "name1=blah; name2 = blubb ;name3\n=foo";
    Map<String, String> map = StringHelper.delimitedToMap(content , ";", "=");
    assertThat(map)
      .containsEntry("name1", "blah")
      .containsEntry("name2", " blubb ")  // white space is not trimmed
      .containsEntry("name3", "foo");
  }
  
  @Test
  public void testDelimitedToArray() {
    String content = "foo, bar, bla,blubb,,bla,   ,bla";
    String[] arr = StringHelper.delimitedToArray(content, ",", true);
    assertThat(arr).containsExactly("foo"," bar"," bla","blubb","","bla","   ","bla");
    arr = StringHelper.delimitedToArray(content, ",", false);
    assertThat(arr).containsExactly("foo"," bar"," bla","blubb","bla","   ","bla");
  }
}
