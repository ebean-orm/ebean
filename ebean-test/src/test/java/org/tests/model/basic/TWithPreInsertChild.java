package org.tests.model.basic;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@Entity
public class TWithPreInsertChild implements TWithPreInsertCommon {

  @Id
  private Integer id;

  @NotNull
  private String name;

  private String title;

  /**
   * For testing what happened in persist controller.
   */
  transient int requestCascadeState;

  public TWithPreInsertChild(String name) {
    this.name = name;
  }

  @Override
  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(String name) {
    this.name = name;
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public void setTitle(String title) {
    this.title = title;
  }

  public int requestCascadeState() {
    return requestCascadeState;
  }

  @Override
  public void requestCascadeState(int requestCascadeState) {
    this.requestCascadeState = requestCascadeState;
  }
}
