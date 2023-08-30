package org.tests.model.docstore;

import io.ebean.annotation.DbJson;
import org.tests.model.basic.BasicDomain;

import javax.persistence.Entity;

/**
 * The reportcontainer itself persists the Report JSON in the database.
 *
 * @author Roland Praml, FOCONIS AG
 */
@Entity
public class ReportContainer extends BasicDomain {

  // By default, ebean will use Jackson as serializer here.
  // It would be great, if ebean will serialize docstore beans automatically with DB.json()
  // This is currently achieved through the JsonSerialize/Deserialize annotations in the report class
  // (ebean -> jackson -> ebean)
  @DbJson(length = 1024 * 1024) // we do not expect report definitions bigger than 1M
  private Report report;


  public Report getReport() {
    return report;
  }

  public void setReport(Report report) {
    this.report = report;
  }
}
