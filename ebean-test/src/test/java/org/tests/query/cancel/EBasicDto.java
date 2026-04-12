package org.tests.query.cancel;

import org.tests.model.basic.EBasic.Status;

/**
 * DTO for Ebasic Queries.
 */
public class EBasicDto {

  private Integer id;
  private Status status;
  private String name;
  private String description;

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

  public String getName() {
    return name;
  }

  public EBasicDto setName(String name) {
    this.name = name;
    return this;
  }

  public String getDescription() {
    return description;
  }

  public EBasicDto setDescription(String description) {
    this.description = description;
    return this;
  }
}
