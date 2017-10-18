package org.tests.model.docstore;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Inheritance;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.ebean.annotation.DocStore;

@DocStore
@Inheritance
@JsonSerialize(using = EbeanJsonSerializer.class)
@JsonDeserialize(using = EbeanJsonDeserializer.class)
public class Report {
  private String title;

  @OneToMany
  private List<Report> embeddedReports;

  @OneToMany
  private List<ReportComment> comments = new ArrayList<>();

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
}
