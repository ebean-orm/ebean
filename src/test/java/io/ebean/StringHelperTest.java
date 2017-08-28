package io.ebean;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import io.ebean.util.StringHelper;

public class StringHelperTest {

  
  @Test
  public void testSplitNames() {
    
    assertThat(StringHelper.splitNames("")).hasSize(0);
    assertThat(StringHelper.splitNames(" , ;")).hasSize(0);
    assertThat(StringHelper.splitNames("foo bar")).containsExactly("foo", "bar");
    assertThat(StringHelper.splitNames("foo, bar")).containsExactly("foo", "bar");
    assertThat(StringHelper.splitNames("foo, bar baz")).containsExactly("foo", "bar", "baz");
    assertThat(StringHelper.splitNames("foo, bar\nbaz")).containsExactly("foo", "bar", "baz");
  }
}
