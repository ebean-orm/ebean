package org.tests.model.docstore;

import io.ebean.annotation.DocStore;

import javax.persistence.Inheritance;
import javax.persistence.OneToMany;
import java.util.List;

@DocStore
@Inheritance
public class Report {
  private String title;

  @OneToMany
  private List<Report> embeddedReports;

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
}
