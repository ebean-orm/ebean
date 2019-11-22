package org.tests.model.docstore;

import java.util.ArrayList;
import java.util.List;
import javax.persistence.Inheritance;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.ebean.annotation.DocStore;
import io.ebean.annotation.JsonIgnore;
import io.ebean.bean.OwnerBeanAware;

@DocStore
@Inheritance
@JsonSerialize(using = EbeanJsonSerializer.class)
@JsonDeserialize(using = EbeanJsonDeserializer.class)
public abstract class Report implements OwnerBeanAware {
  
  private String title;

  @OneToMany
  private List<Report> embeddedReports;

  @OneToMany
  private List<ReportComment> comments = new ArrayList<>();
  
  @Transient
  @JsonIgnore
  private Object parentBean;

  @Transient
  @JsonIgnore
  private String propertyName;

  @Transient
  @JsonIgnore
  private Object additionalKey;
  
  public void setTitle(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }

  public List<Report> getEmbeddedReports() {
    return embeddedReports;
  }

  public void setEmbeddedReports(List<Report> embeddedReports) {
    this.embeddedReports = embeddedReports;
  }

  public List<ReportComment> getComments() {
    return comments;
  }

  public void setComments(List<ReportComment> comments) {
    this.comments = comments;
  }

  @Override
  public void setOwnerBeanInfo(Object parent, String propertyName, Object additionalKey) {
    this.parentBean = parent;
    this.propertyName = propertyName;
    this.additionalKey = additionalKey;
  }

  public Object getParentBean() {
    return parentBean;
  }

  public String getPropertyName() {
    return propertyName;
  }
  
  public Object getAdditionalKey() {
    return additionalKey;
  }
}
