package misc.migration.v1_2;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import io.ebean.annotation.DbComment;
import io.ebean.annotation.History;
import io.ebean.annotation.Index;

@Table(name = "`table`")
@Entity
@History
public class ETable {

  @Column(name = "`index`")
  @DbComment("this is a comment")
  @Id
  private String index;

  @Column(name = "`from`")
  @Index
  private String from;

  @Column(name = "`to`")
  @Index(unique = true)
  private String to;

  @Column(name = "`varchar`")
  @Index(unique = true)
  private String varchar;

  // Note: This column should be removed, (as it is also not present in V1.0)
  // There is a limitation in history generation, that you cannot enable history while you have pending drops
  // History-table will be generyted in V1.3 - SQL without that column, while base table still contains that column
  // When creating the "with_history" view, the DB complains, because the tables do not match.
  // In V1.4 the column will be dropped in base table AND history table.
  // This could be probably fixed, by generating history table also with dropped columns
  @Column(name = "`select`")
  @Index(unique = true)
  private String select;

  @ManyToOne
  @JoinColumn(name = "`foreign`")
  ETable foreign;

  @OneToMany(mappedBy = "foreign")
  List<ETable> foreigns;
}
