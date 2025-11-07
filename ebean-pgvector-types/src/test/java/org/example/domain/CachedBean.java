package org.example.domain;

import com.pgvector.PGbit;
import com.pgvector.PGhalfvec;
import com.pgvector.PGsparsevec;
import com.pgvector.PGvector;
import io.ebean.annotation.Cache;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name="mybean_cached")
@Cache
public class CachedBean extends BaseEntity {
  String name;

  @Column(length = 800)
  PGvector vector;

  @Column(length = 200)
  PGsparsevec sparsevec;

  @Column(length = 200)
  PGbit bit;

  @Column(length = 200)
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

  public PGsparsevec getSparsevec() {
    return sparsevec;
  }

  public void setSparsevec(PGsparsevec sparsevec) {
    this.sparsevec = sparsevec;
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
