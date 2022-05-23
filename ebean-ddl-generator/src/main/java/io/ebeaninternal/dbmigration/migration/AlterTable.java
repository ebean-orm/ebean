package io.ebeaninternal.dbmigration.migration;

import javax.xml.bind.annotation.*;
import java.math.BigInteger;


/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;attGroup ref="{http://ebean-orm.github.io/xml/ns/dbmigration}tablespaceAttributes"/&gt;
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="newName" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="partitionMode" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="partitionColumn" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="identityType" type="{http://ebean-orm.github.io/xml/ns/dbmigration}identityType" /&gt;
 *       &lt;attribute name="identityStart" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" /&gt;
 *       &lt;attribute name="identityIncrement" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" /&gt;
 *       &lt;attribute name="identityCache" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" /&gt;
 *       &lt;attribute name="identityGenerated" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="sequenceName" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="sequenceInitial" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" /&gt;
 *       &lt;attribute name="sequenceAllocate" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" /&gt;
 *       &lt;attribute name="pkName" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="storageEngine" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "alterTable")
public class AlterTable {

  @XmlAttribute(name = "name", required = true)
  protected String name;
  @XmlAttribute(name = "newName")
  protected String newName;
  @XmlAttribute(name = "partitionMode")
  protected String partitionMode;
  @XmlAttribute(name = "partitionColumn")
  protected String partitionColumn;
  @XmlAttribute(name = "identityType")
  protected IdentityType identityType;
  @XmlAttribute(name = "identityStart")
  @XmlSchemaType(name = "positiveInteger")
  protected BigInteger identityStart;
  @XmlAttribute(name = "identityIncrement")
  @XmlSchemaType(name = "positiveInteger")
  protected BigInteger identityIncrement;
  @XmlAttribute(name = "identityCache")
  @XmlSchemaType(name = "positiveInteger")
  protected BigInteger identityCache;
  @XmlAttribute(name = "identityGenerated")
  protected String identityGenerated;
  @XmlAttribute(name = "sequenceName")
  protected String sequenceName;
  @XmlAttribute(name = "sequenceInitial")
  @XmlSchemaType(name = "positiveInteger")
  protected BigInteger sequenceInitial;
  @XmlAttribute(name = "sequenceAllocate")
  @XmlSchemaType(name = "positiveInteger")
  protected BigInteger sequenceAllocate;
  @XmlAttribute(name = "pkName")
  protected String pkName;
  @XmlAttribute(name = "storageEngine")
  protected String storageEngine;
  @XmlAttribute(name = "tablespace")
  protected String tablespace;
  @XmlAttribute(name = "indexTablespace")
  protected String indexTablespace;
  @XmlAttribute(name = "lobTablespace")
  protected String lobTablespace;
  @XmlAttribute(name = "comment")
  protected String comment;

  /**
   * Gets the value of the name property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the value of the name property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setName(String value) {
    this.name = value;
  }

  /**
   * Gets the value of the newName property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getNewName() {
    return newName;
  }

  /**
   * Sets the value of the newName property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setNewName(String value) {
    this.newName = value;
  }

  /**
   * Gets the value of the partitionMode property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getPartitionMode() {
    return partitionMode;
  }

  /**
   * Sets the value of the partitionMode property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setPartitionMode(String value) {
    this.partitionMode = value;
  }

  /**
   * Gets the value of the partitionColumn property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getPartitionColumn() {
    return partitionColumn;
  }

  /**
   * Sets the value of the partitionColumn property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setPartitionColumn(String value) {
    this.partitionColumn = value;
  }

  /**
   * Gets the value of the identityType property.
   *
   * @return possible object is
   * {@link IdentityType }
   */
  public IdentityType getIdentityType() {
    return identityType;
  }

  /**
   * Sets the value of the identityType property.
   *
   * @param value allowed object is
   *              {@link IdentityType }
   */
  public void setIdentityType(IdentityType value) {
    this.identityType = value;
  }

  /**
   * Gets the value of the identityStart property.
   *
   * @return possible object is
   * {@link BigInteger }
   */
  public BigInteger getIdentityStart() {
    return identityStart;
  }

  /**
   * Sets the value of the identityStart property.
   *
   * @param value allowed object is
   *              {@link BigInteger }
   */
  public void setIdentityStart(BigInteger value) {
    this.identityStart = value;
  }

  /**
   * Gets the value of the identityIncrement property.
   *
   * @return possible object is
   * {@link BigInteger }
   */
  public BigInteger getIdentityIncrement() {
    return identityIncrement;
  }

  /**
   * Sets the value of the identityIncrement property.
   *
   * @param value allowed object is
   *              {@link BigInteger }
   */
  public void setIdentityIncrement(BigInteger value) {
    this.identityIncrement = value;
  }

  /**
   * Gets the value of the identityCache property.
   *
   * @return possible object is
   * {@link BigInteger }
   */
  public BigInteger getIdentityCache() {
    return identityCache;
  }

  /**
   * Sets the value of the identityCache property.
   *
   * @param value allowed object is
   *              {@link BigInteger }
   */
  public void setIdentityCache(BigInteger value) {
    this.identityCache = value;
  }

  /**
   * Gets the value of the identityGenerated property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getIdentityGenerated() {
    return identityGenerated;
  }

  /**
   * Sets the value of the identityGenerated property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setIdentityGenerated(String value) {
    this.identityGenerated = value;
  }

  /**
   * Gets the value of the sequenceName property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getSequenceName() {
    return sequenceName;
  }

  /**
   * Sets the value of the sequenceName property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setSequenceName(String value) {
    this.sequenceName = value;
  }

  /**
   * Gets the value of the sequenceInitial property.
   *
   * @return possible object is
   * {@link BigInteger }
   */
  public BigInteger getSequenceInitial() {
    return sequenceInitial;
  }

  /**
   * Sets the value of the sequenceInitial property.
   *
   * @param value allowed object is
   *              {@link BigInteger }
   */
  public void setSequenceInitial(BigInteger value) {
    this.sequenceInitial = value;
  }

  /**
   * Gets the value of the sequenceAllocate property.
   *
   * @return possible object is
   * {@link BigInteger }
   */
  public BigInteger getSequenceAllocate() {
    return sequenceAllocate;
  }

  /**
   * Sets the value of the sequenceAllocate property.
   *
   * @param value allowed object is
   *              {@link BigInteger }
   */
  public void setSequenceAllocate(BigInteger value) {
    this.sequenceAllocate = value;
  }

  /**
   * Gets the value of the pkName property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getPkName() {
    return pkName;
  }

  /**
   * Sets the value of the pkName property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setPkName(String value) {
    this.pkName = value;
  }

  /**
   * Gets the value of the storageEngine property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getStorageEngine() {
    return storageEngine;
  }

  /**
   * Sets the value of the storageEngine property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setStorageEngine(String value) {
    this.storageEngine = value;
  }

  /**
   * Gets the value of the tablespace property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getTablespace() {
    return tablespace;
  }

  /**
   * Sets the value of the tablespace property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setTablespace(String value) {
    this.tablespace = value;
  }

  /**
   * Gets the value of the indexTablespace property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getIndexTablespace() {
    return indexTablespace;
  }

  /**
   * Sets the value of the indexTablespace property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setIndexTablespace(String value) {
    this.indexTablespace = value;
  }

  /**
   * Gets the value of the lobTablespace property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getLobTablespace() {
    return lobTablespace;
  }

  /**
   * Sets the value of the lobTablespace property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setLobTablespace(String value) {
    this.lobTablespace = value;
  }

  /**
   * Gets the value of the comment property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getComment() {
    return comment;
  }

  /**
   * Sets the value of the comment property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setComment(String value) {
    this.comment = value;
  }

}
