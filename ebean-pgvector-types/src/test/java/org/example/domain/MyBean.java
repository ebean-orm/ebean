package org.example.domain;

import com.pgvector.PGbit;
import com.pgvector.PGhalfvec;
import com.pgvector.PGsparsevec;
import com.pgvector.PGvector;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name="mybean")
public class MyBean extends BaseEntity {

  String name;

  @Column(length = 200)
  PGvector vector;

  @Column(length = 350)
  PGsparsevec sparse;

  @Column(length = 1200)
  PGbit bit;

  @Column(length = 420)
  PGhalfvec halfvec;


  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public PGvector getVector() {
    return vector;
  }

  public void setVector(PGvector vector) {
    this.vector = vector;
  }

  public PGsparsevec getSparse() {
    return sparse;
  }

  public void setSparse(PGsparsevec sparse) {
    this.sparse = sparse;
  }

  public PGbit getBit() {
    return bit;
  }

  public void setBit(PGbit bit) {
    this.bit = bit;
  }

  public PGhalfvec getHalfvec() {
    return halfvec;
  }

  public void setHalfvec(PGhalfvec halfvec) {
    this.halfvec = halfvec;
  }
}
