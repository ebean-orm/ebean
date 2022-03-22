package io.ebeaninternal.dbmigration.migration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://ebean-orm.github.io/xml/ns/dbmigration}column" maxOccurs="unbounded"/>
 *         &lt;element ref="{http://ebean-orm.github.io/xml/ns/dbmigration}uniqueConstraint" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element ref="{http://ebean-orm.github.io/xml/ns/dbmigration}foreignKey" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{http://ebean-orm.github.io/xml/ns/dbmigration}tablespaceAttributes"/>
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="partitionMode" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="partitionColumn" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="withHistory" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="draft" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="identityType" type="{http://ebean-orm.github.io/xml/ns/dbmigration}identityType" />
 *       &lt;attribute name="identityStart" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" />
 *       &lt;attribute name="identityIncrement" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" />
 *       &lt;attribute name="identityCache" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" />
 *       &lt;attribute name="identityGenerated" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="sequenceName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="sequenceInitial" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" />
 *       &lt;attribute name="sequenceAllocate" type="{http://www.w3.org/2001/XMLSchema}positiveInteger" />
 *       &lt;attribute name="pkName" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="storageEngine" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
  "column",
  "uniqueConstraint",
  "foreignKey"
})
@XmlRootElement(name = "createTable")
public class CreateTable {

  @XmlElement(required = true)
  protected List<Column> column;
  protected List<UniqueConstraint> uniqueConstraint;
  protected List<ForeignKey> foreignKey;
  @XmlAttribute(name = "name", required = true)
  protected String name;
  @XmlAttribute(name = "partitionMode")
  protected String partitionMode;
  @XmlAttribute(name = "partitionColumn")
  protected String partitionColumn;
  @XmlAttribute(name = "withHistory")
  protected Boolean withHistory;
  @XmlAttribute(name = "draft")
  protected Boolean draft;
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
   * Gets the value of the column property.
   *
   * <p>
   * This accessor method returns a reference to the live list,
   * not a snapshot. Therefore any modification you make to the
   * returned list will be present inside the JAXB object.
   * This is why there is not a <CODE>set</CODE> method for the column property.
   *
   * <p>
   * For example, to add a new item, do as follows:
   * <pre>
   *    getColumn().add(newItem);
   * </pre>
   *
   *
   * <p>
   * Objects of the following type(s) are allowed in the list
   * {@link Column }
   */
  public List<Column> getColumn() {
    if (column == null) {
      column = new ArrayList<>();
    }
    return this.column;
  }

  /**
   * Gets the value of the uniqueConstraint property.
   *
   * <p>
   * This accessor method returns a reference to the live list,
   * not a snapshot. Therefore any modification you make to the
   * returned list will be present inside the JAXB object.
   * This is why there is not a <CODE>set</CODE> method for the uniqueConstraint property.
   *
   * <p>
   * For example, to add a new item, do as follows:
   * <pre>
   *    getUniqueConstraint().add(newItem);
   * </pre>
   *
   *
   * <p>
   * Objects of the following type(s) are allowed in the list
   * {@link UniqueConstraint }
   */
  public List<UniqueConstraint> getUniqueConstraint() {
    if (uniqueConstraint == null) {
      uniqueConstraint = new ArrayList<>();
    }
    return this.uniqueConstraint;
  }

  /**
   * Gets the value of the foreignKey property.
   *
   * <p>
   * This accessor method returns a reference to the live list,
   * not a snapshot. Therefore any modification you make to the
   * returned list will be present inside the JAXB object.
   * This is why there is not a <CODE>set</CODE> method for the foreignKey property.
   *
   * <p>
   * For example, to add a new item, do as follows:
   * <pre>
   *    getForeignKey().add(newItem);
   * </pre>
   *
   *
   * <p>
   * Objects of the following type(s) are allowed in the list
   * {@link ForeignKey }
   */
  public List<ForeignKey> getForeignKey() {
    if (foreignKey == null) {
      foreignKey = new ArrayList<>();
    }
    return this.foreignKey;
  }

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
   * Gets the value of the withHistory property.
   *
   * @return possible object is
   * {@link Boolean }
   */
  public Boolean isWithHistory() {
    return withHistory;
  }

  /**
   * Sets the value of the withHistory property.
   *
   * @param value allowed object is
   *              {@link Boolean }
   */
  public void setWithHistory(Boolean value) {
    this.withHistory = value;
  }

  /**
   * Gets the value of the draft property.
   *
   * @return possible object is
   * {@link Boolean }
   */
  public Boolean isDraft() {
    return draft;
  }

  /**
   * Sets the value of the draft property.
   *
   * @param value allowed object is
   *              {@link Boolean }
   */
  public void setDraft(Boolean value) {
    this.draft = value;
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
