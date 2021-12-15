package org.tests.o2o;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.OneToOne;

@Entity
public class OtoLevelBLazy {

  @Id
  private Long id;

  private String name;

  @ManyToMany()
  private List<OtoLevelC> c;

  @OneToOne()
  private OtoLevelALazy a;

  @Lob
  private String blob;

  public OtoLevelBLazy(String name) {
    this.name = name;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public OtoLevelALazy getA() {
    return a;
  }

  public void setA(OtoLevelALazy a) {
    this.a = a;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<OtoLevelC> getC() {
    return c;
  }

  public final void _ebean_onPersistTrigger(String trt) {
      recalc();
  }

  protected void recalc() {
    this.getBlob();
  }

  public String getBlob() {
    return blob;
  }

  public void setBlob(String blob) {
    this.blob = blob;
  }

}
