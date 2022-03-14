package io.ebean.config;

import org.slf4j.MDC;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Propagates MDC context for tasks executed in the background.
 */
public final class MdcBackgroundExecutorWrapper implements BackgroundExecutorWrapper {

  /**
   * Wrap the task with MDC context if defined.
   */
  @Override
  public <T> Callable<T> wrap(Callable<T> task) {
    final Map<String, String> map = MDC.getCopyOfContextMap();
    if (map == null) {
      return task;
    } else {
      return () -> {
        MDC.setContextMap(map);
        try {
          return task.call();
        } finally {
          MDC.clear();
        }
      };
    }
  }

  /**
   * Wrap the task with MDC context if defined.
   */
  @Override
  public Runnable wrap(Runnable task) {
    final Map<String, String> map = MDC.getCopyOfContextMap();
    if (map == null) {
      return task;
    } else {
      return () -> {
        MDC.setContextMap(map);
        try {
          task.run();
        } finally {
          MDC.clear();
        }
      };
    }
  }
}
