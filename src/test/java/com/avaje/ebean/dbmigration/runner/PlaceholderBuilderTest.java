package com.avaje.ebean.dbmigration.runner;

import org.assertj.core.data.MapEntry;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


public class PlaceholderBuilderTest {

  @Test
  public void empty() throws Exception {

    assertThat(PlaceholderBuilder.build(null, null)).isEmpty();
  }

  @Test
  @SuppressWarnings("unchecked")
  public void comma() throws Exception {

    assertThat(PlaceholderBuilder.build("a=1", null))
        .containsExactly(MapEntry.entry("a","1"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void comma_withSpace() throws Exception {

    assertThat(PlaceholderBuilder.build(" a=1 ", null))
        .containsExactly(MapEntry.entry("a","1"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void comma_withSpace_withSemi() throws Exception {

    assertThat(PlaceholderBuilder.build(" a=1 ; ", null))
        .containsExactly(MapEntry.entry("a","1"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void commaPair() throws Exception {

    assertThat(PlaceholderBuilder.build("a=1;b=2", null))
        .containsExactly(MapEntry.entry("a","1"),MapEntry.entry("b","2"));
  }


  @Test
  @SuppressWarnings("unchecked")
  public void mapPair() throws Exception {

    Map<String,String> in = new HashMap<String,String>();
    in.put("a", "1");
    in.put("b", "2");

    assertThat(PlaceholderBuilder.build(null, in))
        .containsExactly(MapEntry.entry("a","1"),MapEntry.entry("b","2"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void mapPair_override() throws Exception {

    Map<String,String> in = new HashMap<String,String>();
    in.put("a", "1");
    in.put("b", "2");

    assertThat(PlaceholderBuilder.build("a=11;b=12", in))
        .containsExactly(MapEntry.entry("a","1"),MapEntry.entry("b","2"));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void mapPair_join() throws Exception {

    Map<String,String> in = new HashMap<String,String>();
    in.put("c", "3");

    assertThat(PlaceholderBuilder.build("a=1;b=2", in))
        .containsExactly(MapEntry.entry("a","1"),MapEntry.entry("b","2"), MapEntry.entry("c","3"));
  }
}