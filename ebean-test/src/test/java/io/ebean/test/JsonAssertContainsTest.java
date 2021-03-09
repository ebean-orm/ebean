package io.ebean.test;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.Test;

import java.util.stream.Stream;

import static io.ebean.test.Json.readNodeFromResource;
import static org.assertj.core.api.Assertions.assertThat;


public class JsonAssertContainsTest {

  @Test
  public void assertContains_itself() {
    JsonNode original = readNodeFromResource("/contains/original.json");
    JsonAssertContains.assertContains(original, original);
  }

  @Test
  public void assertContains_subset() {
    JsonNode original = readNodeFromResource("/contains/original.json");
    JsonNode expected = readNodeFromResource("/contains/original-subset.json");
    JsonAssertContains.assertContains(original, expected);
  }


  @Test
  public void testContainsFails() {
    JsonNode original = readNodeFromResource("/contains/original.json");
    JsonNode expected = readNodeFromResource("/contains/original-subset-modified.json");
    try {
      JsonAssertContains.assertContains(original, expected);
    } catch (AssertionError e) {
      String exceptionMessage = e.getMessage();
      Stream.of("Expected field 'someString1' to be equal to '\"aaaa\"' but was '\"string1\"",
        "Expected field 'someValue1' to be equal to '99' but was '1'",
        "Expected field 'someArray1[0]' to be of type 'STRING' but was 'NUMBER",
        "Expected field 'someArray2[0].value1' to be of type 'ARRAY' but was 'NUMBER'",
        "Expected field 'someArray2[0].value2' to be of type 'OBJECT' but was 'STRING'",
        "Expected field 'someArray2[0].array1[0]' to be '\"1\"' but was null",
        "Expected field 'someArray2[0].object1.val5' to be present",
        "Expected field 'someArray2[0].object1.val6' to be present",
        "Expected field 'someArray2[0].object2' to be of type 'NULL' but was 'OBJECT'",
        "Expected field 'someArray2[0].objectNull' to be of type 'OBJECT' but was 'NULL'")
        .forEach(assertionError -> assertThat(exceptionMessage).contains(assertionError));
    }
  }


  @Test
  public void assertContains_checkNull() {
    JsonNode original = readNodeFromResource("/contains/check-null-actual.json");
    JsonNode expected = readNodeFromResource("/contains/check-null-expected.json");
    try {
      JsonAssertContains.assertContains(original, expected);
    } catch (AssertionError e) {
      String exceptionMessage = e.getMessage();
      Stream.of("Expected field 'someNull' to be of type 'NULL' but was 'STRING'",
        "Expected field 'extra' to be present")
        .forEach(assertionError -> assertThat(exceptionMessage).contains(assertionError));
    }
  }

  @Test
  public void assertContains_checkType() {
    JsonNode original = readNodeFromResource("/contains/check-type-actual.json");
    JsonNode expected = readNodeFromResource("/contains/check-type-expected.json");
    try {
      JsonAssertContains.assertContains(original, expected);
    } catch (AssertionError e) {
      String exceptionMessage = e.getMessage();
      Stream.of("Expected field 'some' to be of type 'NUMBER' but was 'STRING'")
        .forEach(assertionError -> assertThat(exceptionMessage).contains(assertionError));
    }
  }

  @Test
  public void path_when_empty() {
    JsonAssertContains contains = new JsonAssertContains();

    assertThat(contains.path()).isEqualTo("");
    assertThat(contains.path("a")).isEqualTo("a");
    assertThat(contains.path("b")).isEqualTo("b");
  }
}
