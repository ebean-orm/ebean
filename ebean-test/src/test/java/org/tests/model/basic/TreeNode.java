package org.tests.model.basic;

import io.ebean.annotation.Formula;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.List;

/**
 * @author Roland Praml, FOCONIS AG
 */
@Entity
public class TreeNode {

  @Id
  private int id;

  @ManyToOne
  private TreeNode parent;
  @OneToMany
  private List<TreeNode> children;

  private int softRef;

  @Formula(select = "${ta}_ref.id", join = "left join e_basic ${ta}_ref on ${ta}_ref.id = ${ta}.soft_ref")
  @ManyToOne
  private EBasic ref;

}
