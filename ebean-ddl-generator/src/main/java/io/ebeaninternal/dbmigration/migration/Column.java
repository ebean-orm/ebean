package io.ebeaninternal.dbmigration.migration;

import jakarta.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="before" type="{http://ebean-orm.github.io/xml/ns/dbmigration}ddl-script" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="after" type="{http://ebean-orm.github.io/xml/ns/dbmigration}ddl-script" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="type" use="required" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="defaultValue" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="notnull" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="historyExclude" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="primaryKey" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="identity" type="{http://www.w3.org/2001/XMLSchema}boolean" /&gt;
 *       &lt;attribute name="checkConstraint" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="checkConstraintName" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="unique" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="uniqueOneToOne" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="references" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="foreignKeyName" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="foreignKeyIndex" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="foreignKeyOnDelete" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="foreignKeyOnUpdate" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="comment" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
  "before",
  "after"
})
@XmlRootElement(name = "column")
public class Column {

  protected List<DdlScript> before;
  protected List<DdlScript> after;
  @XmlAttribute(name = "name", required = true)
  protected String name;
  @XmlAttribute(name = "type", required = true)
  protected String type;
  @XmlAttribute(name = "defaultValue")
  protected String defaultValue;
  @XmlAttribute(name = "notnull")
  protected Boolean notnull;
  @XmlAttribute(name = "historyExclude")
  protected Boolean historyExclude;
  @XmlAttribute(name = "primaryKey")
  protected Boolean primaryKey;
  @XmlAttribute(name = "identity")
  protected Boolean identity;
  @XmlAttribute(name = "checkConstraint")
  protected String checkConstraint;
  @XmlAttribute(name = "checkConstraintName")
  protected String checkConstraintName;
  @XmlAttribute(name = "unique")
  protected String unique;
  @XmlAttribute(name = "uniqueOneToOne")
  protected String uniqueOneToOne;
  @XmlAttribute(name = "references")
  protected String references;
  @XmlAttribute(name = "foreignKeyName")
  protected String foreignKeyName;
  @XmlAttribute(name = "foreignKeyIndex")
  protected String foreignKeyIndex;
  @XmlAttribute(name = "foreignKeyOnDelete")
  protected String foreignKeyOnDelete;
  @XmlAttribute(name = "foreignKeyOnUpdate")
  protected String foreignKeyOnUpdate;
  @XmlAttribute(name = "comment")
  protected String comment;

  /**
   * Gets the value of the before property.
   *
   * <p>
   * This accessor method returns a reference to the live list,
   * not a snapshot. Therefore any modification you make to the
   * returned list will be present inside the Jakarta XML Binding object.
   * This is why there is not a <CODE>set</CODE> method for the before property.
   *
   * <p>
   * For example, to add a new item, do as follows:
   * <pre>
   *    getBefore().add(newItem);
   * </pre>
   *
   *
   * <p>
   * Objects of the following type(s) are allowed in the list
   * {@link DdlScript }
   */
  public List<DdlScript> getBefore() {
    if (before == null) {
      before = new ArrayList<>();
    }
    return this.before;
  }

  /**
   * Gets the value of the after property.
   *
   * <p>
   * This accessor method returns a reference to the live list,
   * not a snapshot. Therefore any modification you make to the
   * returned list will be present inside the Jakarta XML Binding object.
   * This is why there is not a <CODE>set</CODE> method for the after property.
   *
   * <p>
   * For example, to add a new item, do as follows:
   * <pre>
   *    getAfter().add(newItem);
   * </pre>
   *
   *
   * <p>
   * Objects of the following type(s) are allowed in the list
   * {@link DdlScript }
   */
  public List<DdlScript> getAfter() {
    if (after == null) {
      after = new ArrayList<>();
    }
    return this.after;
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
   * Gets the value of the type property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getType() {
    return type;
  }

  /**
   * Sets the value of the type property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setType(String value) {
    this.type = value;
  }

  /**
   * Gets the value of the defaultValue property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getDefaultValue() {
    return defaultValue;
  }

  /**
   * Sets the value of the defaultValue property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setDefaultValue(String value) {
    this.defaultValue = value;
  }

  /**
   * Gets the value of the notnull property.
   *
   * @return possible object is
   * {@link Boolean }
   */
  public Boolean isNotnull() {
    return notnull;
  }

  /**
   * Sets the value of the notnull property.
   *
   * @param value allowed object is
   *              {@link Boolean }
   */
  public void setNotnull(Boolean value) {
    this.notnull = value;
  }

  /**
   * Gets the value of the historyExclude property.
   *
   * @return possible object is
   * {@link Boolean }
   */
  public Boolean isHistoryExclude() {
    return historyExclude;
  }

  /**
   * Sets the value of the historyExclude property.
   *
   * @param value allowed object is
   *              {@link Boolean }
   */
  public void setHistoryExclude(Boolean value) {
    this.historyExclude = value;
  }

  /**
   * Gets the value of the primaryKey property.
   *
   * @return possible object is
   * {@link Boolean }
   */
  public Boolean isPrimaryKey() {
    return primaryKey;
  }

  /**
   * Sets the value of the primaryKey property.
   *
   * @param value allowed object is
   *              {@link Boolean }
   */
  public void setPrimaryKey(Boolean value) {
    this.primaryKey = value;
  }

  /**
   * Gets the value of the identity property.
   *
   * @return possible object is
   * {@link Boolean }
   */
  public Boolean isIdentity() {
    return identity;
  }

  /**
   * Sets the value of the identity property.
   *
   * @param value allowed object is
   *              {@link Boolean }
   */
  public void setIdentity(Boolean value) {
    this.identity = value;
  }

  /**
   * Gets the value of the checkConstraint property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getCheckConstraint() {
    return checkConstraint;
  }

  /**
   * Sets the value of the checkConstraint property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setCheckConstraint(String value) {
    this.checkConstraint = value;
  }

  /**
   * Gets the value of the checkConstraintName property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getCheckConstraintName() {
    return checkConstraintName;
  }

  /**
   * Sets the value of the checkConstraintName property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setCheckConstraintName(String value) {
    this.checkConstraintName = value;
  }

  /**
   * Gets the value of the unique property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getUnique() {
    return unique;
  }

  /**
   * Sets the value of the unique property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setUnique(String value) {
    this.unique = value;
  }

  /**
   * Gets the value of the uniqueOneToOne property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getUniqueOneToOne() {
    return uniqueOneToOne;
  }

  /**
   * Sets the value of the uniqueOneToOne property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setUniqueOneToOne(String value) {
    this.uniqueOneToOne = value;
  }

  /**
   * Gets the value of the references property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getReferences() {
    return references;
  }

  /**
   * Sets the value of the references property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setReferences(String value) {
    this.references = value;
  }

  /**
   * Gets the value of the foreignKeyName property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getForeignKeyName() {
    return foreignKeyName;
  }

  /**
   * Sets the value of the foreignKeyName property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setForeignKeyName(String value) {
    this.foreignKeyName = value;
  }

  /**
   * Gets the value of the foreignKeyIndex property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getForeignKeyIndex() {
    return foreignKeyIndex;
  }

  /**
   * Sets the value of the foreignKeyIndex property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setForeignKeyIndex(String value) {
    this.foreignKeyIndex = value;
  }

  /**
   * Gets the value of the foreignKeyOnDelete property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getForeignKeyOnDelete() {
    return foreignKeyOnDelete;
  }

  /**
   * Sets the value of the foreignKeyOnDelete property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setForeignKeyOnDelete(String value) {
    this.foreignKeyOnDelete = value;
  }

  /**
   * Gets the value of the foreignKeyOnUpdate property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getForeignKeyOnUpdate() {
    return foreignKeyOnUpdate;
  }

  /**
   * Sets the value of the foreignKeyOnUpdate property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setForeignKeyOnUpdate(String value) {
    this.foreignKeyOnUpdate = value;
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
