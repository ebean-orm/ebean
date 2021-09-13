package org.tests.query.cancel;

import org.tests.model.basic.EBasic.Status;

/**
 * DTO for Ebasic Queries.
 */
public class EBasicDto {
  private Integer id;

  private Status status;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public Status getStatus() {
    return status;
  }

  public void setStatus(Status status) {
    this.status = status;
  }
}