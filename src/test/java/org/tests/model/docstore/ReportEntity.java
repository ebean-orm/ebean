package org.tests.model.docstore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Entity;

import org.tests.model.basic.BasicDomain;

import io.ebean.annotation.DbJson;

@Entity
public class ReportEntity extends BasicDomain {
  private static final long serialVersionUID = 1L;

  @DbJson
  private Report report;
  
  @DbJson
  private List<Report> reports = new ArrayList<>();

  @DbJson
  private Map<String, Report> reportMap = new HashMap<>();
  
  public Report getReport() {
    return report;
  }

  public void setReport(Report report) {
    this.report = report;
  }
  
  public List<Report> getReports() {
    return reports;
  }
  
  public Map<String, Report> getReportMap() {
    return reportMap;
  }
}
