package org.tests.model.docstore;

import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.tests.model.basic.Customer;

import io.ebean.annotation.DocStore;
import io.ebean.annotation.JsonIgnore;
import io.ebean.bean.OwnerBeanAware;

@DocStore
public class ReportComment implements OwnerBeanAware {



  private String comment;

  @ManyToOne
  private Customer author;

  @Transient
  @JsonIgnore
  private Object parentBean;

  @Transient
  @JsonIgnore
  private String propertyName;

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public Customer getAuthor() {
    return author;
  }

  public void setAuthor(Customer author) {
    this.author = author;
  }

  @Override
  public void setOwnerBeanInfo(Object parent, String propertyName) {
    this.parentBean = parent;
    this.propertyName = propertyName;
  }

  public Object getParentBean() {
    return parentBean;
  }

  public String getPropertyName() {
    return propertyName;
  }
}
