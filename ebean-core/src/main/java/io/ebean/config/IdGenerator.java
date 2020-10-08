package io.ebean.config;

/**
 * A customer Id generator that can be registered with Ebean and
 * assigned to @Id properties using the name attribute of @GeneratedValue.
 */
public interface IdGenerator {

  /**
   * Return the next Id value.
   */
  Object nextValue();

  /**
   * Return the name of the IdGenerator.
   * <p>
   * The name is used to assign the IdGenerator to a property using
   * <code>@GeneratedValue(name="myGeneratorName")</code>
   * </p>
   */
  String getName();
}
