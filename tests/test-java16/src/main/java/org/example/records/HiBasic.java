package org.example.records;

import jakarta.persistence.*;

import java.util.SequencedMap;
import java.util.SequencedSet;

@Entity
public class HiBasic {

  @Id
  long id;

  @OneToMany(cascade = CascadeType.ALL)
  SequencedSet<HiSeq> seqs;

  @MapKey(name="key")
  @OneToMany(cascade = CascadeType.ALL)
  SequencedMap<String, HiMap> map;

  public long id() {
    return id;
  }

  public HiBasic setId(long id) {
    this.id = id;
    return this;
  }

  public SequencedSet<HiSeq> seqs() {
    return seqs;
  }

  public HiBasic setSeqs(SequencedSet<HiSeq> seqs) {
    this.seqs = seqs;
    return this;
  }

  public SequencedMap<String, HiMap> map() {
    return map;
  }

  public HiBasic setMap(SequencedMap<String, HiMap> map) {
    this.map = map;
    return this;
  }
}
