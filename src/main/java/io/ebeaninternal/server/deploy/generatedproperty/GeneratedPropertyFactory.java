package io.ebeaninternal.server.deploy.generatedproperty;

import io.ebean.Transaction;
import io.ebean.config.ClassLoadConfig;
import io.ebean.config.CurrentUserProvider;
import io.ebean.config.IdGenerator;
import io.ebean.config.ServerConfig;
import io.ebean.config.dbplatform.PlatformIdGenerator;
import io.ebeaninternal.server.deploy.meta.DeployBeanProperty;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of GeneratedPropertyFactory.
 */
public class GeneratedPropertyFactory {

  private final CounterFactory counterFactory = new CounterFactory();

  private final InsertTimestampFactory insertFactory;

  private final UpdateTimestampFactory updateFactory;

  private final HashSet<String> numberTypes = new HashSet<>();

  private final GeneratedWhoModified generatedWhoModified;

  private final GeneratedWhoCreated generatedWhoCreated;

  private final ClassLoadConfig classLoadConfig;

  private final Map<String, PlatformIdGenerator> idGeneratorMap = new HashMap<>();

  public GeneratedPropertyFactory(boolean offlineMode, ServerConfig serverConfig, List<IdGenerator> idGenerators) {

    this.classLoadConfig = serverConfig.getClassLoadConfig();
    this.insertFactory = new InsertTimestampFactory(classLoadConfig);
    this.updateFactory = new UpdateTimestampFactory(classLoadConfig);

    CurrentUserProvider currentUserProvider = serverConfig.getCurrentUserProvider();
    if (currentUserProvider != null) {
      generatedWhoCreated = new GeneratedWhoCreated(currentUserProvider);
      generatedWhoModified = new GeneratedWhoModified(currentUserProvider);
    } else if (offlineMode) {
      currentUserProvider = new DummyCurrentUser();
      generatedWhoCreated = new GeneratedWhoCreated(currentUserProvider);
      generatedWhoModified = new GeneratedWhoModified(currentUserProvider);
    } else {
      generatedWhoCreated = null;
      generatedWhoModified = null;
    }

    numberTypes.add(Integer.class.getName());
    numberTypes.add(int.class.getName());
    numberTypes.add(Long.class.getName());
    numberTypes.add(long.class.getName());
    numberTypes.add(Short.class.getName());
    numberTypes.add(short.class.getName());
    numberTypes.add(Double.class.getName());
    numberTypes.add(double.class.getName());
    numberTypes.add(BigDecimal.class.getName());

    if (idGenerators != null) {
      for (IdGenerator idGenerator : idGenerators) {
        idGeneratorMap.put(idGenerator.getName(), new CustomIdGenerator(idGenerator));
      }
    }
  }

  public ClassLoadConfig getClassLoadConfig() {
    return classLoadConfig;
  }

  private boolean isNumberType(String typeClassName) {
    return numberTypes.contains(typeClassName);
  }

  public void setVersion(DeployBeanProperty property) {
    if (isNumberType(property.getPropertyType().getName())) {
      setCounter(property);
    } else {
      setUpdateTimestamp(property);
    }
  }

  public void setCounter(DeployBeanProperty property) {

    counterFactory.setCounter(property);
  }

  public void setInsertTimestamp(DeployBeanProperty property) {

    insertFactory.setInsertTimestamp(property);
  }

  public void setUpdateTimestamp(DeployBeanProperty property) {

    updateFactory.setUpdateTimestamp(property);
  }

  public void setWhoCreated(DeployBeanProperty property) {
    if (generatedWhoCreated == null) {
      throw new IllegalStateException("No CurrentUserProvider has been set so @WhoCreated is not supported");
    }
    property.setGeneratedProperty(generatedWhoCreated);
  }

  public void setWhoModified(DeployBeanProperty property) {
    if (generatedWhoModified == null) {
      throw new IllegalStateException("No CurrentUserProvider has been set so @WhoModified is not supported");
    }
    property.setGeneratedProperty(generatedWhoModified);
  }

  /**
   * Return the named custom IdGenerator (wrapped as a PlatformIdGenerator).
   */
  public PlatformIdGenerator getIdGenerator(String generatorName) {
    return idGeneratorMap.get(generatorName);
  }

  /**
   * Wraps the custom IdGenerator to implement PlatformIdGenerator.
   */
  private static class CustomIdGenerator implements PlatformIdGenerator {

    private final IdGenerator generator;

    CustomIdGenerator(IdGenerator generator) {
      this.generator = generator;
    }

    @Override
    public String getName() {
      return generator.getName();
    }

    @Override
    public boolean isDbSequence() {
      return false;
    }

    @Override
    public Object nextId(Transaction transaction) {
      return generator.nextValue();
    }

    @Override
    public void preAllocateIds(int allocateSize) {
      // do nothing
    }
  }

  private static class DummyCurrentUser implements CurrentUserProvider {
    @Override
    public Object currentUser() {
      throw new RuntimeException("never called");
    }
  }
}
