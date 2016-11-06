package com.avaje.tests.model.onetoone.album;

import com.avaje.ebean.Model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;

@Entity
public class Album extends BaseModel {

  public static final Model.Finder<Long, Album> find = new Model.Finder<>(Album.class);

  private String name;

  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
  private Cover cover;

  public Album(String name) {
    this.name = name;
  }

  public Album() {
  }

  public String getName() {
    return this.name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Cover getCover() {
    return this.cover;
  }

  public void setCover(Cover cover) {
    this.cover = cover;
  }
}
