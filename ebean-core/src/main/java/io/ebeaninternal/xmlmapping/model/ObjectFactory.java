package io.ebeaninternal.xmlmapping.model;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each
 * Java content interface and Java element interface
 * generated in the io.ebeaninternal.xmlmapping.model package.
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
   * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: io.ebeaninternal.xmlmapping.model
   */
  public ObjectFactory() {
  }

  /**
   * Create an instance of {@link XmRawSql }
   */
  public XmRawSql createRawSql() {
    return new XmRawSql();
  }

  /**
   * Create an instance of {@link XmAliasMapping }
   */
  public XmAliasMapping createAliasMapping() {
    return new XmAliasMapping();
  }

  /**
   * Create an instance of {@link XmColumnMapping }
   */
  public XmColumnMapping createColumnMapping() {
    return new XmColumnMapping();
  }

  /**
   * Create an instance of {@link XmQuery }
   */
  public XmQuery createQuery() {
    return new XmQuery();
  }

  /**
   * Create an instance of {@link XmEbean }
   */
  public XmEbean createEbean() {
    return new XmEbean();
  }

  /**
   * Create an instance of {@link XmEntity }
   */
  public XmEntity createEntity() {
    return new XmEntity();
  }

  /**
   * Create an instance of {@link XmNamedQuery }
   */
  public XmNamedQuery createNamedQuery() {
    return new XmNamedQuery();
  }

}
