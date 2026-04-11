package io.ebean.test;

import io.ebean.Database;
import io.ebean.bean.EntityBean;
import io.ebeaninternal.server.deploy.BeanDescriptor;
import io.ebeaninternal.server.deploy.BeanProperty;
import io.ebeaninternal.server.deploy.BeanPropertyAssocMany;
import io.ebeaninternal.server.deploy.BeanPropertyAssocOne;

import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Builds entity bean instances with randomly populated scalar fields for use in tests.
 * <p>
 * {@code @Id} and {@code @Version} properties are left at their defaults (zero/null).
 * {@code @ManyToOne} and {@code @OneToOne} relationships that have cascade persist are
 * recursively built and set. Collection relationships ({@code @OneToMany},
 * {@code @ManyToMany}) are left empty — the caller can populate them if needed.
 * </p>
 * <pre>{@code
 *   TestEntityBuilder builder = TestEntityBuilder.builder(DB.getDefault()).build();
 *
 *   // build in-memory (not saved to database)
 *   MyEntity entity = builder.build(MyEntity.class);
 *
 *   // build and insert to the database
 *   MyEntity entity = builder.save(MyEntity.class);
 *
 *   // supply a custom value generator for domain-specific values
 *   TestEntityBuilder builder = TestEntityBuilder.builder(DB.getDefault())
 *       .valueGenerator(myGenerator)
 *       .build();
 * }</pre>
 */
public class TestEntityBuilder {

  private final Database database;
  private final RandomValueGenerator valueGenerator;

  private TestEntityBuilder(Database database, RandomValueGenerator valueGenerator) {
    this.database = database;
    this.valueGenerator = valueGenerator;
  }

  /** Returns a new {@link Builder} for the given database. */
  public static Builder builder(Database database) {
    return new Builder(database);
  }

  /** Builder for {@link TestEntityBuilder}. */
  public static final class Builder {

    private final Database database;
    private RandomValueGenerator valueGenerator;

    private Builder(Database database) {
      this.database = requireNonNull(database);
    }

    /** Override the default {@link RandomValueGenerator}, e.g. for domain-specific value generation. */
    public Builder valueGenerator(RandomValueGenerator valueGenerator) {
      this.valueGenerator = valueGenerator;
      return this;
    }

    /** Build and return a {@link TestEntityBuilder}. */
    public TestEntityBuilder build() {
      if (valueGenerator == null) {
        valueGenerator = new RandomValueGenerator();
      }
      return new TestEntityBuilder(database, valueGenerator);
    }
  }

  /**
   * Build and return an instance of the entity class with scalar fields populated
   * with random values. The entity is not saved to the database.
   *
   * @param beanClass the entity class to build
   * @throws IllegalArgumentException if the class is not a known Ebean entity
   */
  public <T> T build(Class<T> beanClass) {
    return build(beanClass, new HashSet<>());
  }

  /**
   * Build an instance of the entity class, insert it to the database, and return it.
   *
   * @param beanClass the entity class to build and save
   * @throws IllegalArgumentException if the class is not a known Ebean entity
   */
  public <T> T save(Class<T> beanClass) {
    T bean = build(beanClass);
    database.save(bean);
    return bean;
  }

  private <T> T build(Class<T> beanClass, Set<Class<?>> buildStack) {
    BeanDescriptor<T> descriptor = (BeanDescriptor<T>) database.pluginApi().beanType(beanClass);
    if (descriptor == null) {
      throw new IllegalArgumentException("No BeanDescriptor found for " + beanClass.getName()
        + " — is it an @Entity registered with this Database?");
    }

    Set<String> importedSaveNames = importedSavePropertyNames(descriptor);

    T bean = descriptor.createBean();
    buildStack.add(beanClass);
    try {
      for (BeanProperty prop : descriptor.propertiesAll()) {
        if (prop.isId() || prop.isVersion() || prop.isGenerated() || prop.isTransient()) {
          continue;
        }
        if (prop instanceof BeanPropertyAssocMany) {
          // leave collections empty — caller populates if needed
          continue;
        }
        if (prop instanceof BeanPropertyAssocOne) {
          if (importedSaveNames.contains(prop.name())) {
            BeanPropertyAssocOne<?> assocOne = (BeanPropertyAssocOne<?>) prop;
            Class<?> targetType = assocOne.targetType();
            if (!buildStack.contains(targetType)) {
              Object related = build(targetType, buildStack);
              prop.setValue((EntityBean) bean, related);
            }
            // else: cycle detected — leave the reference null
          }
          // non-cascade-save association — leave null
          continue;
        }
        // scalar property
        Object value = valueGenerator.generate(prop);
        if (value != null) {
          prop.setValue((EntityBean) bean, value);
        }
      }
    } finally {
      buildStack.remove(beanClass);
    }
    return bean;
  }

  private Set<String> importedSavePropertyNames(BeanDescriptor<?> descriptor) {
    Set<String> names = new HashSet<>();
    for (BeanPropertyAssocOne<?> p : descriptor.propertiesOneImportedSave()) {
      names.add(p.name());
    }
    return names;
  }
}
