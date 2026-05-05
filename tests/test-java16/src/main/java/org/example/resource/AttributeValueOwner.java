package org.example.resource;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import org.example.records.BaseModel;

import java.util.List;

@Entity
public class AttributeValueOwner extends BaseModel {
  @OneToMany(cascade = CascadeType.ALL)
  private List<AttributeValue> attributeValues;

  public List<AttributeValue> getAttributeValues() {
    return attributeValues;
  }

  public void setAttributeValues(List<AttributeValue> attributeValues) {
    this.attributeValues = attributeValues;
  }

  public void addAttributeValue(AttributeValue attributeValue){
    getAttributeValues().add(attributeValue);
    attributeValue.setAttributeValueOwner(this);
  }
}
