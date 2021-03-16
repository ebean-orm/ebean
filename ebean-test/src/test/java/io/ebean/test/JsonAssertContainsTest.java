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
    } catch (AssertionError e) {
      String exceptionMessage = e.getMessage();
      System.out.println(exceptionMessage);
      Stream.of("Expected field 'someString1' to be equal to '\"aaaa\"' but was '\"string1\"",
        "Expected field 'someValue1' to be equal to '99' but was '1'",
        "Expected field 'someObject1.value1' to be of type 'ARRAY' but was 'NUMBER'",
        "Expected field 'someObject1.value2' to be of type 'OBJECT' but was 'STRING'",
        "Expected array element 'someObject1.array1[0]' was not matched to an element in the actual array - element: 101",
        "Actual array element 'someObject1.array1[0]' was not matched to an element in the expected array - element: 99",
        "Expected field 'someObject1.object1.val5' to be present",
        "Expected field 'someObject1.object1.val6' to be present",
        "Expected field 'someObject1.object2' to be of type 'NULL' but was 'OBJECT'",
        "Expected field 'someObject1.objectNull' to be of type 'OBJECT' but was 'NULL'")
        .forEach(assertionError -> assertThat(exceptionMessage).contains(assertionError));
      return;
    }
    Assertions.fail("Expected an exception to be thrown");
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
      return;
    }
    Assertions.fail("Expected an exception to be thrown");
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
      return;
    }
    Assertions.fail("Expected an exception to be thrown");
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
    } catch (AssertionError e) {
      String exceptionMessage = e.getMessage();
      Stream.of("Unmatched array size for '', expected 6 but got 4 elements",
        "Expected array element '[2]' was not matched to an element in the actual array - element: {\"c\":3}",
        "Expected array element '[4]' was not matched to an element in the actual array - element: {\"e\":5}",
        "Expected array element '[5]' was not matched to an element in the actual array - element: {\"f\":6}",
        "Actual array element '[3]' was not matched to an element in the expected array - element: {\"b\":2}")
        .forEach(assertionError -> assertThat(exceptionMessage).contains(assertionError));
      return;
    }

    Assertions.fail("Expected an exception to be thrown");
  }

  @Test
  public void assertNestedArray_ordering() {
    JsonNode original = readNodeFromResource("/contains/nested-array.json");
    JsonNode actual = readNodeFromResource("/contains/nested-array-ordering.json");
    JsonAssertContains.assertContains(actual, original);
  }

  @Test
  public void assertNestedArray_both() {
    try {
      JsonNode original = readNodeFromResource("/contains/nested-array.json");
      JsonNode actual = readNodeFromResource("/contains/nested-array-both.json");
      JsonAssertContains.assertContains(actual, original);
    } catch (AssertionError e) {
      String exceptionMessage = e.getMessage();
      Stream.of(
        "Expected array element 'someArray1[1]' was not matched to an element in the actual array - element: {\"name\":\"b\"}",
        "Actual array element 'someArray1[0]' was not matched to an element in the expected array - element: {\"name\":\"z\"}")
        .forEach(assertionError -> assertThat(exceptionMessage).contains(assertionError));
      return;
    }

    Assertions.fail("Expected an exception to be thrown");
  }

  @Test
  public void assertNestedArray_extraExpected() {
    try {
      JsonNode original = readNodeFromResource("/contains/nested-array.json");
      JsonNode actual = readNodeFromResource("/contains/nested-array-extraExpected.json");
      JsonAssertContains.assertContains(actual, original);
    } catch (AssertionError e) {
      String exceptionMessage = e.getMessage();
      Stream.of(
        "Unmatched array size for 'someArray1', expected 3 but got 4 elements",
        "Actual array element 'someArray1[2]' was not matched to an element in the expected array - element: {\"name\":\"d\"}")
        .forEach(assertionError -> assertThat(exceptionMessage).contains(assertionError));
      return;
    }

    Assertions.fail("Expected an exception to be thrown");
  }

  @Test
  public void assertNestedArray_missExpected() {
    try {
      JsonNode original = readNodeFromResource("/contains/nested-array.json");
      JsonNode actual = readNodeFromResource("/contains/nested-array-missExpected.json");
      JsonAssertContains.assertContains(actual, original);
    } catch (AssertionError e) {
      String exceptionMessage = e.getMessage();
      Stream.of(
        "Unmatched array size for 'someArray1', expected 3 but got 2 elements",
        "Expected array element 'someArray1[1]' was not matched to an element in the actual array - element: {\"name\":\"b\"}")
        .forEach(assertionError -> assertThat(exceptionMessage).contains(assertionError));
      return;
    }

    Assertions.fail("Expected an exception to be thrown");
  }
}
