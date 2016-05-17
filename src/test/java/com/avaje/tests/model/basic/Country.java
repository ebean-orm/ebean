package com.avaje.tests.model.basic;

import com.avaje.ebean.annotation.Cache;
import com.avaje.ebean.annotation.CacheBeanTuning;
import com.avaje.ebean.annotation.ChangeLog;
import com.avaje.ebean.annotation.ChangeLogInsertMode;
import com.avaje.ebean.annotation.DocStore;
import com.avaje.ebean.annotation.ReadAudit;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;

/**
 * Country entity bean.
 */
@DocStore
@ReadAudit
@ChangeLog(inserts = ChangeLogInsertMode.INCLUDE)
@Cache(readOnly = true, enableQueryCache = true)
@CacheBeanTuning(maxSize = 500)
@Entity
@Table(name = "o_country")
public class Country {

  @Id
  @Size(max = 2)
  String code;

  @Size(max = 60)
  String name;

  public String toString() {
    return code;
  }

  /**
   * Return code.
   */
  public String getCode() {
    return code;
  }

  /**
   * Set code.
   */
  public void setCode(String code) {
    this.code = code;
  }

  /**
   * Return name.
   */
  public String getName() {
    return name;
  }

  /**
   * Set name.
   */
  public void setName(String name) {
    this.name = name;
  }


}
