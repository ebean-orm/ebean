package org.tests.resource;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
public class AttributeValue extends BaseModel {
  private String stringValue;
  private int intValue;

  @ManyToOne(cascade = CascadeType.ALL)
  private Label name;
  
  @ManyToOne
  private AttributeDescriptor attributeDescriptor;

  @ManyToOne
  private AttributeValueOwner attributeValueOwner;

  public AttributeValueOwner getAttributeValueOwner() {
    return attributeValueOwner;
  }

  public void setAttributeValueOwner(AttributeValueOwner attributeValueOwner) {
    this.attributeValueOwner = attributeValueOwner;
  }

  public AttributeValue() {
  }

  public AttributeValue(Label name, String stringValue, AttributeDescriptor attributeDescriptor) {
	this.name = name;
	this.stringValue = stringValue;
    this.attributeDescriptor = attributeDescriptor;
  }

  public AttributeValue(Label name, int intValue, AttributeDescriptor attributeDescriptor) {
		this.name = name;
    this.intValue = intValue;
    this.attributeDescriptor = attributeDescriptor;
  }

  public String getStringValue() {
    return stringValue;
  }

  public void setStringValue(String stringValue) {
    this.stringValue = stringValue;
  }

  public int getIntValue() {
    return intValue;
  }

  public void setIntValue(int intValue) {
    this.intValue = intValue;
  }

  public AttributeDescriptor getAttributeDescriptor() {
    return attributeDescriptor;
  }

  public void setAttributeDescriptor(AttributeDescriptor attributeDescriptor) {
    this.attributeDescriptor = attributeDescriptor;
  }
  
  public Label getName() {
	return name;
  }

  public void setName(Label name) {
	this.name = name;
  }
}
