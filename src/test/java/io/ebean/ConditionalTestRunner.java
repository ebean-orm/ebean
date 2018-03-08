package io.ebean;

import io.ebean.annotation.ForPlatform;
import io.ebean.annotation.IgnorePlatform;
import io.ebean.annotation.Platform;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

/**
 * This testrunner checks for an {@link IgnorePlatform} annotation and ignores the test.
 *
 * @author Roland Praml, FOCONIS AG
 */
public class ConditionalTestRunner extends BlockJUnit4ClassRunner {
  public ConditionalTestRunner(Class<?> klass) throws InitializationError {
    super(klass);
  }
  @Override
  public void runChild(FrameworkMethod method, RunNotifier notifier) {
    ForPlatform forPlatform = method.getAnnotation(ForPlatform.class);
    if (forPlatform != null) {
      if (!platformMath(forPlatform.value())) {
        notifier.fireTestIgnored(describeChild(method));
        return;
      }
    }

    IgnorePlatform ignore = method.getAnnotation(IgnorePlatform.class);

    if (ignore == null || !platformMath(ignore.value())) {
      super.runChild(method, notifier);
    } else {
      notifier.fireTestIgnored(describeChild(method));
    }

  }
  private boolean platformMath(Platform[] platforms) {
    Platform current = Ebean.getDefaultServer().getPluginApi().getDatabasePlatform().getPlatform();
    for (Platform p : platforms) {
      if (p.equals(current)) {
        return true;
      }
      if (p == Platform.SQLSERVER && current == Platform.SQLSERVER17) {
        // treat SQLSERVER as SQLSERVER17 in testing
        return true;
      }
    }
    return false;
  }
}
