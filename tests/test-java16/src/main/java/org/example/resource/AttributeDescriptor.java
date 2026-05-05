package org.example.resource;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import org.example.records.BaseModel;

@Entity
public class AttributeDescriptor extends BaseModel {

  @ManyToOne(cascade = CascadeType.ALL)
  private Label name;

  @ManyToOne(cascade = CascadeType.ALL)
  private Label description;

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
}
