package org.example.resource;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import org.example.records.BaseModel;

import java.util.Locale;

@Entity
public class LabelText extends BaseModel {
  private String localeText;

  private Locale locale;

  @ManyToOne
  private Label label;

  public LabelText(){
  }

  public LabelText(Locale locale, String localeText) {
    this.locale = locale;
    this.localeText = localeText;
  }

  public Locale getLocale() {
    return locale;
  }

  public void setLocale(Locale locale) {
    this.locale = locale;
  }

  public String getLocaleText() {
    return localeText;
  }

  public Label getLabel() {
    return label;
  }

  public void setLabel(Label label) {
    this.label = label;
  }

  public void setLocaleText(String localeText) {
    this.localeText = localeText;
  }
}
