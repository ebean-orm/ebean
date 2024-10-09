package org.tests.model.m2m;

import io.ebean.annotation.Identity;
import io.ebean.annotation.Platform;
import io.ebean.annotation.Where;

import jakarta.persistence.*;
import java.util.List;

@Identity(start = 1000)
@Entity
public class MnyNode {

  @Id
  Integer id;

  String name;

  @ManyToMany(cascade = CascadeType.ALL)
  @JoinTable(name = "mny_edge",
    joinColumns = @JoinColumn(name = "from_id", referencedColumnName = "id"),
    inverseJoinColumns = @JoinColumn(name = "to_id", referencedColumnName = "id"))
  @Where(clause = "${mta}.flags != 12345 and '${dbTableName}' = 'mny_node'")
  List<MnyNode> allRelations;

  @ManyToMany(cascade = CascadeType.ALL)
  @JoinTable(name = "mny_edge",
    joinColumns = @JoinColumn(name = "to_id", referencedColumnName = "id"),
    inverseJoinColumns = @JoinColumn(name = "from_id", referencedColumnName = "id"))
  List<MnyNode> allReverseRelations;

  @ManyToMany
  @JoinTable(name = "mny_edge",
    joinColumns = @JoinColumn(name = "from_id", referencedColumnName = "id"),
    inverseJoinColumns = @JoinColumn(name = "to_id", referencedColumnName = "id"))
  @Where(clause = "${mta}.flags & 1 != 0")
  @Where(clause = "BITAND(${mta}.flags, 1) != 0", platforms = {Platform.H2, Platform.ORACLE})
  List<MnyNode> bit1Relations;

  @ManyToMany
  @JoinTable(name = "mny_edge",
    joinColumns = @JoinColumn(name = "to_id", referencedColumnName = "id"),
    inverseJoinColumns = @JoinColumn(name = "from_id", referencedColumnName = "id"))
  @Where(clause = "${mta}.flags & 1 != 0")
  @Where(clause = "BITAND(${mta}.flags, 1) != 0", platforms = {Platform.H2, Platform.ORACLE})
  List<MnyNode> bit1ReverseRelations;

  @ManyToMany
  @JoinTable(name = "mny_edge",
    joinColumns = @JoinColumn(name = "from_id", referencedColumnName = "id"),
    inverseJoinColumns = @JoinColumn(name = "to_id", referencedColumnName = "id"))
  @Where(clause = "${mta}.flags & 2 != 0")
  @Where(clause = "BITAND(${mta}.flags, 2) != 0", platforms = {Platform.H2, Platform.ORACLE})
  List<MnyNode> bit2Relations;

  @ManyToMany
  @JoinTable(name = "mny_edge",
    joinColumns = @JoinColumn(name = "to_id", referencedColumnName = "id"),
    inverseJoinColumns = @JoinColumn(name = "from_id", referencedColumnName = "id"))
  @Where(clause = "${mta}.flags & 2 != 0")
  @Where(clause = "BITAND(${mta}.flags, 2) != 0", platforms = {Platform.H2, Platform.ORACLE})
  List<MnyNode> bit2ReverseRelations;

  @ManyToMany
  @JoinTable(name = "mny_edge",
    joinColumns = @JoinColumn(name = "to_id", referencedColumnName = "id"),
    inverseJoinColumns = @JoinColumn(name = "from_id", referencedColumnName = "id"))
  @Where(clause = "'${dbTableName}' = ${ta}.name")
  List<MnyNode> withDbTableName;

  public MnyNode() {

  }

  public MnyNode(String name) {
    this.name = name;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<MnyNode> getAllRelations() {
    return allRelations;
  }

  public List<MnyNode> getAllReverseRelations() {
    return allReverseRelations;
  }

  public List<MnyNode> getBit1Relations() {
    return bit1Relations;
  }

  public List<MnyNode> getBit1ReverseRelations() {
    return bit1ReverseRelations;
  }

  public List<MnyNode> getBit2Relations() {
    return bit2Relations;
  }

  public List<MnyNode> getBit2ReverseRelations() {
    return bit2ReverseRelations;
  }

  public List<MnyNode> getWithDbTableName() {
    return withDbTableName;
  }

  public void setWithDbTableName(List<MnyNode> withDbTableName) {
    this.withDbTableName = withDbTableName;
  }

}
