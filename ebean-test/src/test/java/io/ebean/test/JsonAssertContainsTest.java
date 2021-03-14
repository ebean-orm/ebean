package io.ebean.test;

import com.fasterxml.jackson.databind.JsonNode;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.util.stream.Stream;

import static io.ebean.test.Json.readNode;
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
      Assertions.fail("Expected an exception to be thrown");
    } catch (AssertionError e) {
      String exceptionMessage = e.getMessage();
      System.out.println(exceptionMessage);
      Stream.of("Expected field 'someString1' to be equal to '\"aaaa\"' but was '\"string1\"",
        "Expected field 'someValue1' to be equal to '99' but was '1'",
        "Unable to match expected element 'someArray1[0]' in the actual array",
        "Expected field 'someObject1.value1' to be of type 'ARRAY' but was 'NUMBER'",
        "Expected field 'someObject1.value2' to be of type 'OBJECT' but was 'STRING'",
        "Unable to match expected element 'someObject1.array1[0]' in the actual array",
        "Expected field 'someObject1.object1.val5' to be present",
        "Expected field 'someObject1.object1.val6' to be present",
        "Expected field 'someObject1.object2' to be of type 'NULL' but was 'OBJECT'",
        "Expected field 'someObject1.objectNull' to be of type 'OBJECT' but was 'NULL'")
        .forEach(assertionError -> assertThat(exceptionMessage).contains(assertionError));
    }
  }


  @Test
  public void assertContains_checkNull() {
    JsonNode original = readNodeFromResource("/contains/check-null-actual.json");
    JsonNode expected = readNodeFromResource("/contains/check-null-expected.json");
    try {
      JsonAssertContains.assertContains(original, expected);
      Assertions.fail("Expected an exception to be thrown");
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
      Assertions.fail("Expected an exception to be thrown");
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


  @Test
  public void assertContainsNumbersArrayShuffled() {
    JsonNode array = readNode("[2, 54, 13, 10]");
    JsonNode arrayShuffled = readNode("[2, 13, 10, 54]");

    JsonAssertContains.assertContains(arrayShuffled, array);
  }

  @Test
  public void assertContainsObjectsArrayShuffled() {
    JsonNode array = readNodeFromResource("/contains/array-objects.json");
    JsonNode arrayShuffled = readNodeFromResource("/contains/array-objects-shuffled.json");

    JsonAssertContains.assertContains(arrayShuffled, array);
  }

  @Test
  public void assertArrayElementsNotFound() {
    JsonNode original = readNodeFromResource("/contains/array-multi-match.json");
    JsonNode actual = readNodeFromResource("/contains/array-multi-match-duplicate-props.json");

    try {
      JsonAssertContains.assertContains(actual, original);
      Assertions.fail("Expected an exception to be thrown");
    } catch (AssertionError e) {
      System.out.println(e);
      String exceptionMessage = e.getMessage();
      Stream.of("Unable to match expected element '[5]' in the actual array",
        "Unable to match expected element '[4]' in the actual array")
        .forEach(assertionError -> assertThat(exceptionMessage).contains(assertionError));
    }
  }
}
