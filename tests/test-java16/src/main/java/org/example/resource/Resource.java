package org.example.resource;

import jakarta.persistence.*;
import org.example.records.BaseModel;

@Entity
public class Resource extends BaseModel {
  @ManyToOne(cascade = CascadeType.ALL)
  private Label name;

  @ManyToOne(cascade = CascadeType.ALL)
  private Label description;

  private String resourceId;

  @ManyToOne(cascade = CascadeType.ALL)
  private AttributeValueOwner attributeValueOwner;

  public Label getName() {
    return name;
  }

  public void setName(Label name) {
    this.name = name;
  }

  public Label getDescription() {
    return description;
  }

  public void setDescription(Label description) {
    this.description = description;
  }

  public AttributeValueOwner getAttributeValueOwner() {
    return attributeValueOwner;
  }

  public void setAttributeValueOwner(AttributeValueOwner attributeValueOwner) {
    this.attributeValueOwner = attributeValueOwner;
  }

  public String getResourceId() {
    return resourceId;
  }

  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }
}
