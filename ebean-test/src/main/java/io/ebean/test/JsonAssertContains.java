package io.ebean.test;

import com.fasterxml.jackson.databind.JsonNode;
import org.assertj.core.api.Assertions;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Perform traversal of JsonNodes comparing against an expected JsonNode that
 * typically contains a subset of the data (typically excludes any generated properties
 * like when modified timestamps etc).
 */
public class JsonAssertContains {

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

    Map<Integer, Set<Integer>> matchingIndexes = findMatchingIndexes(actualJsonNode, expectedJsonNode);
    List<Integer> unmatchedIndexes = listUnmatchedIndexes(expectedJsonNode.size(), matchingIndexes);
    List<Map.Entry<Integer, Set<Integer>>> remainingEntries = removeMultipleMatches(matchingIndexes);
    if (!remainingEntries.isEmpty()) {
      unmatchedIndexes.addAll(remainingEntries.stream().map(Map.Entry::getKey).collect(Collectors.toList()));
    }

    List<String> errors = unmatchedIndexes.stream()
      .map(index -> String.format("Unable to match expected element '%s[%d]' in the actual array", path(), index))
      .collect(Collectors.toList());

    return CompareResult.errors(errors);
  }

  private List<Integer> listUnmatchedIndexes(int size, Map<Integer, Set<Integer>> matchingIndexes) {
    List<Integer> unmatched = new LinkedList<>();
    for (int i = 0; i < size; i++) {
      if (!matchingIndexes.containsKey(i)) {
        unmatched.add(i);
      }
    }
    return unmatched;
  }

  private List<Map.Entry<Integer, Set<Integer>>> removeMultipleMatches(Map<Integer, Set<Integer>> matchingIndexes) {
    List<Map.Entry<Integer, Set<Integer>>> entries = new ArrayList<>(matchingIndexes.entrySet());
    entries.sort(Comparator.comparingInt(entry -> entry.getValue().size()));
    ListIterator<Map.Entry<Integer, Set<Integer>>> iterator = entries.listIterator();

    while (iterator.hasNext()) {
      Map.Entry<Integer, Set<Integer>> next = iterator.next();
      if (!next.getValue().isEmpty()) {
        iterator.remove();
        Integer aMatchingIndex = next.getValue().stream().findFirst().get();
        removeAllMatchingIndexesOf(aMatchingIndex, entries);
      }
    }

    return entries;
  }

  private void removeAllMatchingIndexesOf(Integer aMatchingIndex, List<Map.Entry<Integer, Set<Integer>>> matchingIndexes) {
    matchingIndexes.forEach(entry -> entry.getValue().remove(aMatchingIndex));
  }

  private Map<Integer, Set<Integer>> findMatchingIndexes(JsonNode actualJsonNode, JsonNode expectedJsonNode) {
    Map<Integer, Set<Integer>> matchingElementsIndexes = new HashMap<>();
    for (int e = 0; e < expectedJsonNode.size(); e++) {
      for (int a = 0; a < actualJsonNode.size(); a++) {
        CompareResult result = checkRecursive("[" + e + "]", actualJsonNode.get(a), expectedJsonNode.get(e));
        if (result.isApplicable() && !result.hasErrors()) {
          matchingElementsIndexes.computeIfAbsent(e, key -> new HashSet<>()).add(a);
        }
      }
    }
    return matchingElementsIndexes;
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
}
