package org.tests.o2o;


import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
public class OtoLevelALazy {

  @Id
  private Long id;

  private String name;

  @OneToOne(mappedBy = "a", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  private OtoLevelBLazy b;

  public OtoLevelALazy(String name) {
    this.name = name;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public OtoLevelBLazy getB() {
    return b;
  }

  public void setB(OtoLevelBLazy b) {
    this.b = b;
  }
  
  public void setName(String name) {
	  this.name = name;
  }
}
