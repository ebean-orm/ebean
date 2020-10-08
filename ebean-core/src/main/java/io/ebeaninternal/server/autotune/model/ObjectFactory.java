package io.ebeaninternal.server.autotune.model;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each
 * Java content interface and Java element interface
 * generated in the io.ebeaninternal.server.autotune.model package.
 * <p>An ObjectFactory allows you to programatically
 * construct new instances of the Java representation
 * for XML content. The Java representation of XML
 * content can consist of schema derived interfaces
 * and classes representing the binding of schema
 * type definitions, element declarations and model
 * groups.  Factory methods for each of these are
 * provided in this class.
 */
@XmlRegistry
public class ObjectFactory {


  /**
   * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: io.ebeaninternal.server.autotune.model
   */
  public ObjectFactory() {
  }

  /**
   * Create an instance of {@link ProfileNew }
   */
  public ProfileNew createProfileNew() {
    return new ProfileNew();
  }

  /**
   * Create an instance of {@link Origin }
   */
  public Origin createOrigin() {
    return new Origin();
  }

  /**
   * Create an instance of {@link ProfileEmpty }
   */
  public ProfileEmpty createProfileEmpty() {
    return new ProfileEmpty();
  }

  /**
   * Create an instance of {@link Autotune }
   */
  public Autotune createAutotune() {
    return new Autotune();
  }

  /**
   * Create an instance of {@link ProfileDiff }
   */
  public ProfileDiff createProfileDiff() {
    return new ProfileDiff();
  }

}
