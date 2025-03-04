package org.example.resource;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import org.example.records.BaseModel;

@Entity
public class AttributeValue extends BaseModel {
  private String stringValue;
  private int intValue;

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

  public AttributeValue(String stringValue, AttributeDescriptor attributeDescriptor) {
    this.stringValue = stringValue;
    this.attributeDescriptor = attributeDescriptor;
  }

  public AttributeValue(int intValue, AttributeDescriptor attributeDescriptor) {
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
}
