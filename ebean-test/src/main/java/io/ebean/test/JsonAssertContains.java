package io.ebean.test;

import com.fasterxml.jackson.databind.JsonNode;
import org.assertj.core.api.Assertions;

import java.util.*;

/**
 * Perform traversal of JsonNodes comparing against an expected JsonNode that
 * typically contains a subset of the data (typically excludes any generated properties
 * like when modified timestamps etc).
 */
class JsonAssertContains {

  private final Stack<String> path = new Stack<>();

  static void assertContains(JsonNode actualJsonNode, JsonNode expectedJsonNode) {
    new JsonAssertContains().contains(actualJsonNode, expectedJsonNode);
  }

  private void contains(JsonNode actualJsonNode, JsonNode expectedJsonNode) {
    CompareResult result = checkRecursive(null, actualJsonNode, expectedJsonNode);
    if (result.hasErrors()) {
      List<String> errors = result.getErrors();
      String errorsString = String.join("\n", errors);
      errorsString += "\nExpected JSON fields: " + expectedJsonNode;
      errorsString += "\nActual JSON: " + actualJsonNode;
      Assertions.fail(errorsString);
    }
  }

  private CompareResult checkRecursive(String name, JsonNode actualJsonNode, JsonNode expectedJsonNode) {
    if (name != null) {
      path.push(name);
    }

    CompareResult result = checkNull(actualJsonNode, expectedJsonNode);
    if (result.isApplicable()) {
      return pop(name, result);
    }

    result = checkType(actualJsonNode, expectedJsonNode);
    if (result.isApplicable()) {
      return pop(name, result);
    }

    result = checkArray(actualJsonNode, expectedJsonNode);
    if (result.isApplicable()) {
      return pop(name, result);
    }

    result = checkObject(actualJsonNode, expectedJsonNode);
    if (result.isApplicable()) {
      return pop(name, result);
    }

    result = checkValue(actualJsonNode, expectedJsonNode);
    if (result.isApplicable()) {
      return pop(name, result);
    }

    return CompareResult.NOT_APPLICABLE;
  }

  private CompareResult pop(String name, CompareResult result) {
    if (name != null) {
      path.pop();
    }
    return result;
  }

  private CompareResult checkNull(JsonNode actualJsonNode, JsonNode expectedJsonNode) {
    if (actualJsonNode == null) {
      return CompareResult.error(String.format("Expected field '%s' to be '%s' but was null", path(), expectedJsonNode));
    }
    return CompareResult.NOT_APPLICABLE;
  }

  private CompareResult checkType(JsonNode actualJsonNode, JsonNode expectedJsonNode) {
    if (!expectedJsonNode.getNodeType().equals(actualJsonNode.getNodeType())) {
      return CompareResult.error(String.format("Expected field '%s' to be of type '%s' but was '%s'", path(), expectedJsonNode.getNodeType(), actualJsonNode.getNodeType()));
    }
    return CompareResult.NOT_APPLICABLE;
  }

  private CompareResult checkArray(JsonNode actualJsonNode, JsonNode expectedJsonNode) {
    if (!expectedJsonNode.isArray()) {
      return CompareResult.NOT_APPLICABLE;
    }
    return new MatchArrayElements(actualJsonNode, expectedJsonNode).match();
  }

  private CompareResult checkObject(JsonNode actualJsonNode, JsonNode expectedJsonNode) {
    if (!expectedJsonNode.isObject()) {
      return CompareResult.NOT_APPLICABLE;
    }
    List<String> errors = new LinkedList<>();
    Iterator<Map.Entry<String, JsonNode>> expectedFields = expectedJsonNode.fields();
    while (expectedFields.hasNext()) {
      Map.Entry<String, JsonNode> expectedField = expectedFields.next();
      String expectedKey = expectedField.getKey();
      JsonNode actualNode = actualJsonNode.get(expectedKey);
      if (actualNode == null) {
        errors.add(String.format("Expected field '%s' to be present", path(expectedKey)));
      } else {
        CompareResult result = checkRecursive(expectedKey, actualNode, expectedField.getValue());
        errors.addAll(result.getErrors());
      }
    }
    return CompareResult.errors(errors);
  }

  private CompareResult checkValue(JsonNode actualJsonNode, JsonNode expectedJsonNode) {
    if (!expectedJsonNode.equals(actualJsonNode)) {
      return CompareResult.error(String.format("Expected field '%s' to be equal to '%s' but was '%s'", path(), expectedJsonNode, actualJsonNode));
    }
    return CompareResult.NO_ERRORS;
  }

  String path(String expectedKey) {
    if (path.isEmpty()) {
      return expectedKey;
    }
    return path() + "." + expectedKey;
  }

  String path() {
    if (path.isEmpty()) {
      return "";
    }
    return String.join(".", path).replace(".[", "[");
  }

  /**
   * Match two arrays allowing elements to be in a different order.
   */
  private class MatchArrayElements {
    private final JsonNode actualJsonNode;
    private final JsonNode expectedJsonNode;
    private final Map<Integer, JsonNode> expectedMap = new LinkedHashMap<>();
    private final Map<Integer, JsonNode> actualMap = new LinkedHashMap<>();

    MatchArrayElements(JsonNode actualJsonNode, JsonNode expectedJsonNode) {
      this.actualJsonNode = actualJsonNode;
      this.expectedJsonNode = expectedJsonNode;
      for (int e = 0; e < expectedJsonNode.size(); e++) {
        expectedMap.put(e, expectedJsonNode.get(e));
      }
      for (int a = 0; a < actualJsonNode.size(); a++) {
        actualMap.put(a, actualJsonNode.get(a));
      }
    }

    CompareResult match() {
      Iterator<Map.Entry<Integer, JsonNode>> iterator = expectedMap.entrySet().iterator();
      while (iterator.hasNext()) {
        Map.Entry<Integer, JsonNode> expectedEntry = iterator.next();
        int matchPos = findFirstMatch(expectedEntry.getKey(), expectedEntry.getValue());
        if (matchPos > -1) {
          iterator.remove();
        }
      }

      List<String> errors = new ArrayList<>();
      if (actualJsonNode.size() != expectedJsonNode.size()) {
        errors.add(String.format("Unmatched array size for '%s', expected %d but got %d elements", path(), expectedJsonNode.size(), actualJsonNode.size()));
      }
      // unmatched expected -> actual
      for (Map.Entry<Integer, JsonNode> entry : expectedMap.entrySet()) {
        errors.add(String.format("Expected array element '%s[%d]' was not matched to an element in the actual array - element: %s", path(), entry.getKey(), entry.getValue()));
      }
      // unmatched actual -> expected
      for (Map.Entry<Integer, JsonNode> entry : actualMap.entrySet()) {
        errors.add(String.format("Actual array element '%s[%d]' was not matched to an element in the expected array - element: %s", path(), entry.getKey(), entry.getValue()));
      }
      return CompareResult.errors(errors);
    }

    private int findFirstMatch(int e, JsonNode expectedNode) {
      Iterator<Map.Entry<Integer, JsonNode>> iterator = actualMap.entrySet().iterator();
      while (iterator.hasNext()) {
        Map.Entry<Integer, JsonNode> entry = iterator.next();
        CompareResult result = checkRecursive("[" + e + "]", entry.getValue(), expectedNode);
        if (result.isApplicable() && !result.hasErrors()) {
          iterator.remove();
          return entry.getKey();
        }
      }
      return -1;
    }
  }

}
