package io.ebean.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class CompareResult {

  private final boolean applicable;
  private final List<String> errors;

  static final CompareResult NO_ERRORS = new CompareResult(true, Collections.emptyList());
  static final CompareResult NOT_APPLICABLE = new CompareResult(false, Collections.emptyList());

  static CompareResult error(String error) {
    return new CompareResult(true, Collections.singletonList(error));
  }

  static CompareResult errors(List<String> errors) {
    return new CompareResult(true, errors);
  }

  private CompareResult(boolean applicable, List<String> errors) {
    this.applicable = applicable;
    this.errors = new ArrayList<>(errors);
  }

  boolean isApplicable() {
    return applicable;
  }

  boolean hasErrors() {
    return !errors.isEmpty();
  }

  List<String> getErrors() {
    return errors;
  }
}
