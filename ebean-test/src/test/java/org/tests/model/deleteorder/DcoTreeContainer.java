package org.tests.model.deleteorder;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the roots of a {@link DcoTree}, as reported on #1852.
 */
@Entity
public class DcoTreeContainer {

  @Id
  @GeneratedValue
  Long id;

  @OneToMany(cascade = CascadeType.ALL)
  List<DcoTree> trees = new ArrayList<>();

  public Long getId() {
    return id;
  }

  public List<DcoTree> getTrees() {
    return trees;
  }
}
