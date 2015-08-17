
package com.avaje.ebean.dbmigration.migration;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


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
 *         &lt;choice>
 *           &lt;group ref="{http://ebean-orm.github.io/xml/ns/dbmigration}changeSetChildren" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;/choice>
 *       &lt;/sequence>
 *       &lt;attribute name="type" use="required" type="{http://ebean-orm.github.io/xml/ns/dbmigration}changeSetType" />
 *       &lt;attribute name="generated" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="author" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="comment" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "changeSetChildren"
})
@XmlRootElement(name = "changeSet")
public class ChangeSet {

    @XmlElements({
        @XmlElement(name = "configuration", type = Configuration.class),
        @XmlElement(name = "sql", type = Sql.class),
        @XmlElement(name = "createTable", type = CreateTable.class),
        @XmlElement(name = "dropTable", type = DropTable.class),
        @XmlElement(name = "renameTable", type = RenameTable.class),
        @XmlElement(name = "addHistoryTable", type = AddHistoryTable.class),
        @XmlElement(name = "dropHistoryTable", type = DropHistoryTable.class),
        @XmlElement(name = "addColumn", type = AddColumn.class),
        @XmlElement(name = "dropColumn", type = DropColumn.class),
        @XmlElement(name = "alterColumn", type = AlterColumn.class),
        @XmlElement(name = "renameColumn", type = RenameColumn.class),
        @XmlElement(name = "createIndex", type = CreateIndex.class),
        @XmlElement(name = "dropIndex", type = DropIndex.class)
    })
    protected List<Object> changeSetChildren;
    @XmlAttribute(name = "type", required = true)
    protected ChangeSetType type;
    @XmlAttribute(name = "generated")
    protected Boolean generated;
    @XmlAttribute(name = "author")
    protected String author;
    @XmlAttribute(name = "comment")
    protected String comment;

    /**
     * Gets the value of the changeSetChildren property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the changeSetChildren property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getChangeSetChildren().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Configuration }
     * {@link Sql }
     * {@link CreateTable }
     * {@link DropTable }
     * {@link RenameTable }
     * {@link AddHistoryTable }
     * {@link DropHistoryTable }
     * {@link AddColumn }
     * {@link DropColumn }
     * {@link AlterColumn }
     * {@link RenameColumn }
     * {@link CreateIndex }
     * {@link DropIndex }
     * 
     * 
     */
    public List<Object> getChangeSetChildren() {
        if (changeSetChildren == null) {
            changeSetChildren = new ArrayList<Object>();
        }
        return this.changeSetChildren;
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link ChangeSetType }
     *     
     */
    public ChangeSetType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link ChangeSetType }
     *     
     */
    public void setType(ChangeSetType value) {
        this.type = value;
    }

    /**
     * Gets the value of the generated property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isGenerated() {
        return generated;
    }

    /**
     * Sets the value of the generated property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setGenerated(Boolean value) {
        this.generated = value;
    }

    /**
     * Gets the value of the author property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Sets the value of the author property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAuthor(String value) {
        this.author = value;
    }

    /**
     * Gets the value of the comment property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getComment() {
        return comment;
    }

    /**
     * Sets the value of the comment property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setComment(String value) {
        this.comment = value;
    }

}
