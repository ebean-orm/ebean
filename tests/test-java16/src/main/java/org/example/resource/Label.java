package org.example.resource;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import org.example.records.BaseModel;

import java.util.List;
import java.util.Locale;

@Entity
public class Label extends BaseModel  {
  @OneToMany(cascade= CascadeType.ALL)
  private List<LabelText> labelTexts;

  public List<LabelText> getLabelTexts() {
    return labelTexts;
  }

  public void setLabelTexts(List<LabelText> labelTexts) {
    this.labelTexts = labelTexts;
  }

  public LabelText addLabelText(Locale locale, String text){
    LabelText labelText = new LabelText(locale, text);

    getLabelTexts().add(labelText);
    labelText.setLabel(this);
    return labelText;
  }
}
