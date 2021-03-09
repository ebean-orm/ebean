package io.ebean.test;

import com.fasterxml.jackson.databind.JsonNode;
import org.assertj.core.api.Assertions;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;

/**
 * Perform traversal of JsonNodes comparing against an expected JsonNode that
 * typically contains a subset of the data (typically excludes any generated properties
 * like when modified timestamps etc).
 */
class JsonAssertContains {

  private final Stack<String> path = new Stack<>();
  private final LinkedList<String> errors = new LinkedList<>();

  static void assertContains(JsonNode actualJsonNode, JsonNode expectedJsonNode) {
    new JsonAssertContains().contains(actualJsonNode, expectedJsonNode);
  }

  private void contains(JsonNode actualJsonNode, JsonNode expectedJsonNode) {
    checkRecursive(null, actualJsonNode, expectedJsonNode);
    if (!errors.isEmpty()) {
      String errorsString = String.join("\n", errors);
      errorsString += "\nExpected JSON fields: " + expectedJsonNode;
      errorsString += "\nActual JSON: " + actualJsonNode;
      Assertions.fail(errorsString);
    }
  }

  private void checkRecursive(String name, JsonNode actualJsonNode, JsonNode expectedJsonNode) {
    if (name != null) {
      path.push(name);
    }
    if (checkNull(actualJsonNode, expectedJsonNode)) {
      if (checkType(actualJsonNode, expectedJsonNode)) {
        if (checkArray(actualJsonNode, expectedJsonNode)) {
          if (checkObject(actualJsonNode, expectedJsonNode)) {
            checkValue(actualJsonNode, expectedJsonNode);
          }
        }
      }
    }
    if (name != null) {
      path.pop();
    }
  }

  private boolean checkNull(JsonNode actualJsonNode, JsonNode expectedJsonNode) {
    if (actualJsonNode == null) {
      errors.add(String.format("Expected field '%s' to be '%s' but was null", path(), expectedJsonNode));
      return false;
    }
    return true;
  }

  private boolean checkType(JsonNode actualJsonNode, JsonNode expectedJsonNode) {
    if (!expectedJsonNode.getNodeType().equals(actualJsonNode.getNodeType())) {
      errors.add(String.format("Expected field '%s' to be of type '%s' but was '%s'", path(), expectedJsonNode.getNodeType(), actualJsonNode.getNodeType()));
      return false;
    }
    return true;
  }

  private boolean checkArray(JsonNode actualJsonNode, JsonNode expectedJsonNode) {
    if (!expectedJsonNode.isArray()) {
      return true;
    }
    for (int i = 0; i < expectedJsonNode.size(); i++) {
      checkRecursive("[" + i + "]", actualJsonNode.get(i), expectedJsonNode.get(i));
    }
    // do not continue (object or scalar type check)
    return false;
  }

  private boolean checkObject(JsonNode actualJsonNode, JsonNode expectedJsonNode) {
    if (!expectedJsonNode.isObject()) {
      return true;
    }
    Iterator<Map.Entry<String, JsonNode>> expectedFields = expectedJsonNode.fields();
    while (expectedFields.hasNext()) {
      Map.Entry<String, JsonNode> expectedField = expectedFields.next();
      String expectedKey = expectedField.getKey();
      JsonNode actualNode = actualJsonNode.get(expectedKey);
      if (actualNode == null) {
        errors.add(String.format("Expected field '%s' to be present", path(expectedKey)));
      } else {
        checkRecursive(expectedKey, actualNode, expectedField.getValue());
      }
    }
    // do not continue (scalar type check)
    return false;
  }

  private void checkValue(JsonNode actualJsonNode, JsonNode expectedJsonNode) {
    if (!expectedJsonNode.equals(actualJsonNode)) {
      errors.add(String.format("Expected field '%s' to be equal to '%s' but was '%s'", path(), expectedJsonNode, actualJsonNode));
    }
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
}
