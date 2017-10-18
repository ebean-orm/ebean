package org.tests.model.docstore;

import javax.persistence.ManyToOne;

import org.tests.model.basic.Customer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.ebean.annotation.DocStore;

@DocStore
@JsonSerialize(using = EbeanJsonSerializer.class)
@JsonDeserialize(using = EbeanJsonDeserializer.class)
public class ReportComment {
  private String comment;

  @ManyToOne
  private Customer author;

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

}
