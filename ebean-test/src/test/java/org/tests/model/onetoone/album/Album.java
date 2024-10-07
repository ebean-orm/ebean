package org.tests.model.onetoone.album;

import io.ebean.Finder;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToOne;

@Entity
public class Album extends BaseModel {

  public static final Finder<Long, Album> find = new Finder<>(Album.class);

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
