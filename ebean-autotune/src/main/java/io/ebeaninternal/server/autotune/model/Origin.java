package io.ebeaninternal.server.autotune.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * <p>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="callStack" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="key" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="beanType" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="detail" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="original" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
  "callStack"
})
@XmlRootElement(name = "origin")
public class Origin {

  protected String callStack;
  @XmlAttribute(name = "key", required = true)
  protected String key;
  @XmlAttribute(name = "beanType")
  protected String beanType;
  @XmlAttribute(name = "detail")
  protected String detail;
  @XmlAttribute(name = "original")
  protected String original;

  /**
   * Gets the value of the callStack property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getCallStack() {
    return callStack;
  }

  /**
   * Sets the value of the callStack property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setCallStack(String value) {
    this.callStack = value;
  }

  /**
   * Gets the value of the key property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getKey() {
    return key;
  }

  /**
   * Sets the value of the key property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setKey(String value) {
    this.key = value;
  }

  /**
   * Gets the value of the beanType property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getBeanType() {
    return beanType;
  }

  /**
   * Sets the value of the beanType property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setBeanType(String value) {
    this.beanType = value;
  }

  /**
   * Gets the value of the detail property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getDetail() {
    return detail;
  }

  /**
   * Sets the value of the detail property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setDetail(String value) {
    this.detail = value;
  }

  /**
   * Gets the value of the original property.
   *
   * @return possible object is
   * {@link String }
   */
  public String getOriginal() {
    return original;
  }

  /**
   * Sets the value of the original property.
   *
   * @param value allowed object is
   *              {@link String }
   */
  public void setOriginal(String value) {
    this.original = value;
  }

}
