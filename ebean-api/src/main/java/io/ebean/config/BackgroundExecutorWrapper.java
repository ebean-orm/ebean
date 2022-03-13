package io.ebean.config;

import java.util.concurrent.Callable;

/**
 * BackgroundExecutorWrapper that can be used to wrap tasks that are sent to background (i.e. an other thread).
 * It should copy all neccessary thread-local variables. See {@link MdcBackgroundExecutorWrapper} for implementation details.
 * 
 * @author Roland Praml, FOCONIS AG
 */
public interface BackgroundExecutorWrapper {

  /**
   * Wrap the task with MDC context if defined.
   */
  <T> Callable<T> wrap(Callable<T> task);

  /**
   * Wrap the task with MDC context if defined.
   */
  Runnable wrap(Runnable task);

  /**
   * Combines two wrappers by joining them.
   */
  default BackgroundExecutorWrapper with(BackgroundExecutorWrapper inner) {
    return new BackgroundExecutorWrapper() {
      
      @Override
      public Runnable wrap(Runnable task) {
        return BackgroundExecutorWrapper.this.wrap(inner.wrap(task));
      }
      
      @Override
      public <T> Callable<T> wrap(Callable<T> task) {
        return BackgroundExecutorWrapper.this.wrap(inner.wrap(task));
      }
    };
   
  }

}
