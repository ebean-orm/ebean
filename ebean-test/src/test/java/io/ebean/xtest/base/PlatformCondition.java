package io.ebean.xtest.base;

import io.ebean.DB;
import io.ebean.xtest.ForPlatform;
import io.ebean.xtest.IgnorePlatform;
import io.ebean.annotation.Platform;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.AnnotatedElement;
import java.util.Optional;

public class PlatformCondition implements ExecutionCondition {

  private static final ConditionEvaluationResult ENABLED = ConditionEvaluationResult.enabled("ForPlatform is not present");

  @Override
  public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
    Optional<AnnotatedElement> element = context.getElement();
    if (element.isPresent()) {
      final AnnotatedElement annotatedElement = element.get();
      final ForPlatform annotation = annotatedElement.getAnnotation(ForPlatform.class);
      if (annotation != null && !platformMath(annotation.value())) {
        return ConditionEvaluationResult.disabled("@ForPlatform");
      }
      IgnorePlatform ignore = annotatedElement.getAnnotation(IgnorePlatform.class);
      if (ignore != null && platformMath(ignore.value())) {
        return ConditionEvaluationResult.disabled("@IgnorePlatform");
      }
    }
    return ENABLED;
  }

  private boolean platformMath(Platform[] platforms) {
    Platform basePlatform = DB.getDefault().platform().base();
    for (Platform platform : platforms) {
      if (platform.equals(basePlatform)) {
        return true;
      }
    }
    return false;
  }
}
