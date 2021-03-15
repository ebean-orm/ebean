package io.ebean.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class CompareResult {
    private final boolean applicable;
    private final List<String> errors;

    public static final CompareResult NO_ERRORS = new CompareResult(true, Collections.emptyList());
    public static final CompareResult NOT_APPLICABLE = new CompareResult(false, Collections.emptyList());

    public static CompareResult error(String error) {
        return new CompareResult(true, Collections.singletonList(error));
    }

    public static CompareResult errors(List<String> errors) {
        return new CompareResult(true, errors);
    }

    private CompareResult(boolean applicable, List<String> errors) {
        this.applicable = applicable;
        this.errors = new ArrayList<>(errors);
    }

    public boolean isApplicable() {
        return applicable;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public List<String> getErrors() {
        return errors;
    }
}
