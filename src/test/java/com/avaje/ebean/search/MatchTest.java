package com.avaje.ebean.search;


import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class MatchTest {

  Match match() {
    return Match.AND()
        .analyzer("whitespace")
        .boost(2)
        .cutoffFrequency(1)
        .minShouldMatch("50%")
        .maxExpansions(3)
        .zeroTerms("all");
  }

  @Test
  public void equals_when_allSet() {
    assertEquals(match(), match());
    assertEquals(match().hashCode(), match().hashCode());
  }

  @Test
  public void notEquals_when_analyzer() {
    assertNotEquals(match(), match().analyzer("foo"));
    assertNotEquals(match().hashCode(), match().analyzer("foo").hashCode());
    assertNotEquals(match(), match().analyzer(null));
    assertNotEquals(match().hashCode(), match().analyzer(null).hashCode());
  }

  @Test
  public void notEquals_when_boost() {
    assertNotEquals(match(), match().boost(3));
  }

  @Test
  public void notEquals_when_cutoffFrequency() {
    assertNotEquals(match(), match().cutoffFrequency(3));
  }

  @Test
  public void notEquals_when_maxExpansions() {
    assertNotEquals(match(), match().maxExpansions(100));
  }

  @Test
  public void notEquals_when_minShouldMatch() {
    assertNotEquals(match(), match().minShouldMatch("12%"));
  }

  @Test
  public void notEquals_when_zeroTerms() {
    assertNotEquals(match(), match().zeroTerms("none"));
  }

  @Test
  public void notEquals_when_phrase() {
    assertNotEquals(match(), match().phrase());
  }

  @Test
  public void notEquals_when_phrasePrefix() {
    assertNotEquals(match(), match().phrasePrefix());
  }

  @Test
  public void notEquals_when_operator() {
    assertNotEquals(match(), match().opOr());
  }

}