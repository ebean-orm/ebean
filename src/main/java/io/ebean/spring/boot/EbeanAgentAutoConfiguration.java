package io.ebean.spring.boot;

import org.avaje.agentloader.AgentLoader;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;


/**
 * Loads the Ebean enhancement agent early in the Spring Boot startup process,
 * if it is present on the classpath.
 * <p>
 * Note that using this mechanism is only recommended for development;
 * production applications should ideally be enhanced at build time, or at least
 * load the agent via the <code>javaagent</code> JVM option. When the agent is
 * loaded at runtime via this class, any entity classes that have already been
 * loaded won't be enhanced and will fail to work correctly.
 * <p>
 * For unit tests and similar cases where Spring Boot auto-configuration may not
 * be active, loading of the agent can be triggered manually via
 * {@link #enable()}.
 */
@Configuration
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnClass(AgentLoader.class)
public class EbeanAgentAutoConfiguration implements BeanFactoryPostProcessor, PriorityOrdered {

  public EbeanAgentAutoConfiguration() {
    load(); // Spring has already evaluated the @ConditionalOnClass
  }

  @Override
  public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
    // We're not actually doing anything with the BeanFactory, but implementing
    // BeanFactoryPostProcessor ensures we get instantiated early, ideally
    // before anybody has a chance to load any entity classes we want to
    // enhance.
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }

  private static void load() {
    AgentLoader.loadAgentFromClasspath("ebean-agent", "debug=1");
  }

  /**
   * Loads the Ebean agent if the agent-loader and the agent itself are present
   * on the classpath, or does nothing otherwise.
   * <p>
   * Do not call this method from a static initializer as this can lead to a JVM
   * deadlock (the agent attach thread will attempt to acquire the class loader
   * lock, which is held during static initialization).
   */
  public static void enable() {
    try {
      load();
    } catch (NoClassDefFoundError e) {
      /* ignored */
    }
  }
}
